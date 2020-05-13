package online.prostobank.clients.security.keycloak;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.utils.UserInfoDto;
import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static online.prostobank.clients.security.UserRolesConstants.*;
import static online.prostobank.clients.utils.Utils.ROBOT;

/**
 * Утилитный класс для работы с {@link KeycloakSecurityContext}
 * пользователя
 */
@Slf4j
@UtilityClass
public class SecurityContextHelper {
	@Nonnull
	public Set<String> getCurrentUserPermissions() {
		return Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getAuthorities)
				.orElse(new HashSet<>()).stream()
				.map(GrantedAuthority::getAuthority)
				.collect(toSet());
	}

	@Nonnull
	public String getCurrentUsername() {
		Optional<KeycloakSecurityContext> keycloakSecurityContext = getKeycloakSecurityContext();
		if (!keycloakSecurityContext.isPresent()) {
			return ROBOT;
		} else {
			return keycloakSecurityContext
					.map(KeycloakSecurityContext::getToken)
					.map(IDToken::getPreferredUsername)
					.map(username -> {
						log.info("Current username :: {}", username);
						return username;
					})
					.orElseGet(() -> {
						log.info("Unable to get username from RequestContext ");
						return "Unknown";
					});
		}
	}

	@Nonnull
	public Optional<String> getTokenString() {
		return getKeycloakSecurityContext()
				.map(KeycloakSecurityContext::getTokenString);
	}

	private static Optional<KeycloakSecurityContext> getKeycloakSecurityContext() {
		return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.map(ServletRequestAttributes::getRequest)
				.map(request -> (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName()));
	}

	public boolean isCurrentUserGrantedBy(@Nonnull String... roles) {
		return CollectionUtils.containsAll(getCurrentUserPermissions(), asList(roles));
	}

	public boolean isCurrentUserGrantedByAnyOf(@Nonnull String... roles) {
		Set<String> currentUserPermissions = getCurrentUserPermissions();
		return CollectionUtils.containsAny(currentUserPermissions, roles);
	}

	public static Set<String> getRoles(@Nonnull KeycloakAuthenticationToken token) {
		return Optional.of(token)
				.map(AbstractAuthenticationToken::getAuthorities)
				.map(grantedAuthorities -> grantedAuthorities.stream()
						.map(GrantedAuthority::getAuthority)
						.collect(toSet()))
				.orElseGet(Collections::emptySet);
	}

	public static String getKeycloakId(KeycloakAuthenticationToken token) {
		KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
		return principal.getKeycloakSecurityContext().getToken().getSubject();
	}

	public static String getTokenString(KeycloakAuthenticationToken token) {
		KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
		return principal.getKeycloakSecurityContext().getTokenString();
	}

	public static boolean notAllowed(Collection<String> roles, String... filterRoles) {
		return Arrays.stream(filterRoles).anyMatch(roles::contains);
	}

	public static List<String> getAssignedTo(boolean onlySelf, KeycloakAdminClient keycloakAdminClient) {
		String userName = SecurityContextHelper.getCurrentUsername();
		if (onlySelf || SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_FRONT_HOME, ROLE_POS_FRONT_PARTNER, ROLE_POS_OUTER_API_MANAGER)) {
			return Collections.singletonList(userName);
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_ADMIN_HOME)) {
			List<String> users = keycloakAdminClient.usersByRole(ROLE_POS_FRONT_HOME);
			users.add(userName);
			return users;
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_ADMIN_PARTNER)) {
			List<String> users = keycloakAdminClient.usersByRole(ROLE_POS_FRONT_PARTNER);
			users.add(userName);
			return users;
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_OUTER_API_ADMIN)) {
			List<String> users = keycloakAdminClient.usersByRole(ROLE_POS_OUTER_API_MANAGER);
			users.add(userName);
			return users;
		}
		return null;
	}

	public static Set<String> getAllowedRolesForAdmins() {
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_ADMIN)) {
			return ROLES_MANAGERS;
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_ADMIN_HOME)) {
			return ROLES_MANAGERS_HOME;
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_ADMIN_PARTNER)) {
			return ROLES_MANAGERS_PARTNER;
		}
		if (SecurityContextHelper.isCurrentUserGrantedBy(ROLE_POS_OUTER_API_ADMIN)) {
			return ROLES_MANAGERS_OUTER;
		}
		return Collections.emptySet();
	}

	public List<Source> getSources() {
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_FRONT_HOME, ROLE_POS_ADMIN_HOME)) {
			return Collections.singletonList(Source.API_TM_HOME);
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_FRONT_PARTNER, ROLE_POS_ADMIN_PARTNER)) {
			return Collections.singletonList(Source.API_TM_PARTNER);
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_OUTER_API_MANAGER, ROLE_POS_OUTER_API_ADMIN)) {
			return Collections.singletonList(Source.API_OUTER_CALL_CENTER);
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_FRONT)) {
			return asList(
					Source.API_TM,
					Source.API_ANKETA,
					Source.API_ANKETA_V2,
					Source.API_ANKETA_V3,
					Source.API_ANKETA_V4
			);
		}
		return null;
	}

	public static boolean isAllowed(Source source, String assignedTo) {
		String currentUser = getCurrentUsername();
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN_HOME)) {
			return source == Source.API_TM_HOME;
		}
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN_PARTNER)) {
			return source == Source.API_TM_PARTNER;
		}
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_OUTER_API_ADMIN)) {
			return source == Source.API_OUTER_CALL_CENTER;
		}
		if (isCurrentUserGrantedBy(ROLE_POS_FRONT_HOME, ROLE_POS_FRONT_PARTNER, ROLE_POS_OUTER_API_MANAGER)) {
			return currentUser.equals(assignedTo);
		}
		return true;
	}

	public static Optional<Source> getSource() {
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN_HOME, ROLE_POS_FRONT_HOME)) {
			return Optional.of(Source.API_TM_HOME);
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN_PARTNER, ROLE_POS_FRONT_PARTNER)) {
			return Optional.of(Source.API_TM_PARTNER);
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_OUTER_API_ADMIN, ROLE_POS_OUTER_API_MANAGER)) {
			return Optional.of(Source.API_OUTER_CALL_CENTER);
		} else {
			return Optional.empty();
		}
	}

	public static Optional<List<String>> getSourceForColdReport() {
		if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN_HOME)) {
			return Optional.of(Collections.singletonList(Source.API_TM_HOME.name()));
		} else if (isCurrentUserGrantedByAnyOf(ROLE_POS_ADMIN)) {
			return Optional.of(Arrays.stream(Source.values()).map(Source::name).collect(toList()));
		} else {
			return Optional.empty();
		}
	}

	@Nonnull
	public static UserInfoDto getUserInfoDto() {
		return getKeycloakSecurityContext()
				.map(keycloakSecurityContext -> {
					IDToken idToken;
					if (keycloakSecurityContext.getIdToken() == null) {
						idToken = keycloakSecurityContext.getToken();
					} else {
						idToken = keycloakSecurityContext.getIdToken();
					}
					String email = idToken.getEmail();
					String preferredUsername = idToken.getPreferredUsername();
					UUID userId = UUID.fromString(idToken.getSubject());

					return new UserInfoDto(email, preferredUsername, userId);
				})
				.orElseGet(() -> new UserInfoDto(null, ROBOT, null));
	}
}
