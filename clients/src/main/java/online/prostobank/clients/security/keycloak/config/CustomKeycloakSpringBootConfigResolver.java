package online.prostobank.clients.security.keycloak.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Configuration;

/**
 * Решение бага
 * https://issues.jboss.org/browse/KEYCLOAK-8444.
 * при прямой инициализации бина {@link KeycloakSpringBootConfigResolver}
 * происходит делегирование резолва и возникает
 */
@Configuration
public class CustomKeycloakSpringBootConfigResolver extends KeycloakSpringBootConfigResolver {
}
