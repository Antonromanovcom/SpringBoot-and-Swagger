package online.prostobank.clients.security.keycloak;

import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Специальный {@link KeycloakAuthenticationProvider}
 * предусматривающий добавление {@link GrantedAuthority} из
 * любых дополнительных источников помимо
 * {@link CustomKeycloakAuthenticationProvider#addKeycloakRoles}
 * в {@link CustomKeycloakAuthenticationProvider#addUserSpecificAuthorities}
 */
@Slf4j
@RequiredArgsConstructor
public class CustomKeycloakAuthenticationProvider extends KeycloakAuthenticationProvider {

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    /**
     * Основной аунтификационный метод провайдера
     * @param authentication {@link Authentication} содержит токен и принципал аунтификационного запроса
     * @return {@link Authentication} аунтификация
     * @throws AuthenticationException все аунтификационные исключения
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
        AccessToken.Access realmAccess = token
                .getAccount()
                .getKeycloakSecurityContext()
                .getToken()
                .getRealmAccess();
        log.debug(" Realm access :: {}",
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(realmAccess)
        );

        Stream<? extends GrantedAuthority>  resourceAccess = token
                .getAccount()
                .getKeycloakSecurityContext()
                .getToken()
                .getResourceAccess()
                .values().stream()
                .flatMap(it -> addKeycloakRoles(it).stream());

        Collection<? extends GrantedAuthority> keycloakAuthorities =
                // опциональный маппинг GrantedAuthority
                //mapAuthorities(
                Stream.concat(addKeycloakRoles(realmAccess).stream(), resourceAccess)
                        .collect(Collectors.toSet());
        //);

        Collection<? extends GrantedAuthority> grantedAuthorities =
                addUserSpecificAuthorities(authentication, keycloakAuthorities);

        return new KeycloakAuthenticationToken(
                token.getAccount(),
                token.isInteractive(),
                grantedAuthorities
        );
    }

    /**
     * Добавление дополнительных пользовательских {@link GrantedAuthority}
     * @param authentication {@link org.springframework.security.core.Authentication}
     * @param authorities {@link GrantedAuthority}
     * @return {@link GrantedAuthority}
     */
    protected Collection<? extends GrantedAuthority> addUserSpecificAuthorities(
            Authentication authentication,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new ArrayList<>(authorities);
    }

    /**
     * Добавление основных {@link KeycloakRole} из keycloak
     * @return {@link KeycloakRole}
     * @param realmAccess
     */
    protected Collection<? extends GrantedAuthority> addKeycloakRoles(AccessToken.Access realmAccess) {
        return Optional.ofNullable(realmAccess)
                .map(it ->
                        it.getRoles().stream()
                                .map(KeycloakRole::new)
                                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * {@link AuthenticationProvider#supports(java.lang.Class)}
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return KeycloakAuthenticationToken.class.isAssignableFrom(aClass);
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return grantedAuthoritiesMapper != null
                ? grantedAuthoritiesMapper.mapAuthorities(authorities)
                : authorities;
    }
}
