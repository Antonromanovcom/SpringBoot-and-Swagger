package online.prostobank.clients.security;

import club.apibank.connectors.KeycloakApiConnector;
import club.apibank.connectors.KeycloakApiConnectorImpl;
import club.apibank.connectors.domain.KeyCloakToken;
import club.apibank.connectors.domain.RegisterUserDTO;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * Использовать {@link SecurityContextHelper}
 * */
@Deprecated
@RequiredArgsConstructor
@Service
public class UserService  {
	private final RoleServiceProperties config;

	private KeycloakApiConnector connector;
	private KeyCloakToken        keyCloakToken;
	private LocalDateTime        lastKeyCloakRequest;

	private void refreshKeycloakToken() {
		connector = new KeycloakApiConnectorImpl(config.getKeyCloakServerUrl()+ "/realms/" + config.getKeyCloakRealm());
		keyCloakToken = connector.getToken(
				config.getLogin(),
				config.getSecret()
		);
	}

	private boolean needUpdateTokenByTime(Long refreshRate) {
		if (lastKeyCloakRequest == null) {
			return true;
		}
		return ChronoUnit.SECONDS.between(lastKeyCloakRequest, LocalDateTime.now()) >= refreshRate;
	}

	private boolean needUpdateTokenByTime(Integer expiresIn) {
		if (expiresIn == null) {
			return true;
		}
		return needUpdateTokenByTime(expiresIn.longValue());
	}

	public Map<String, Object> registerKeycloakUser(RegisterUserDTO userDTO, Set<String> roles) {
		prepareConnector();
		Map<String, Object> result = connector.registerUser(userDTO, keyCloakToken.getAccess_token(), roles);
		lastKeyCloakRequest = LocalDateTime.now();
		return result;
	}

	public int setUserRoles(String userId, Set<String> roles) {
		prepareConnector();
		int result = connector.setUserRoles(keyCloakToken.getAccess_token(), userId, roles);
		lastKeyCloakRequest = LocalDateTime.now();
		return result;
	}

	public int deleteUserRoles(String userId, Set<String> roles) {
		prepareConnector();
		int result = connector.deleteUserRoles(keyCloakToken.getAccess_token(), userId, roles);
		lastKeyCloakRequest = LocalDateTime.now();
		return result;
	}

	private void prepareConnector() {
		if (keyCloakToken == null || needUpdateTokenByTime(keyCloakToken.getExpires_in())) {
			refreshKeycloakToken();
		}
		connector = new KeycloakApiConnectorImpl(config.getKeyCloakServerUrl() + "/admin/realms/" + config.getKeyCloakRealm());
	}

	public String getAccessToken() {
		if (keyCloakToken == null || needUpdateTokenByTime(keyCloakToken.getExpires_in())) {
			refreshKeycloakToken();
		}
		return keyCloakToken.getAccess_token();
	}
}
