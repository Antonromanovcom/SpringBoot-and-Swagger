package online.prostobank.clients.security.keycloak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class CustomGlobalMethodSecurity extends GlobalMethodSecurityConfiguration {

    /**
     * Переопределение глобального префикса ролей
     */
    protected AccessDecisionManager accessDecisionManager() {
        AffirmativeBased accessDecisionManager = (AffirmativeBased) super.accessDecisionManager();

        accessDecisionManager.getDecisionVoters().stream()
                .filter(RoleVoter.class::isInstance)
                .map(RoleVoter.class::cast)
                .forEach(it -> it.setRolePrefix(""));

        return accessDecisionManager;
    }
}
