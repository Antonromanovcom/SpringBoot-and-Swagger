package online.prostobank.clients.security.keycloak.config;

import lombok.val;
import online.prostobank.clients.security.keycloak.SecurityProperties;
import online.prostobank.clients.security.keycloak.strategy.DomainAwarePermissionEvaluator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;

import static online.prostobank.clients.security.UserRolesConstants.ROLES_ALL;

@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true
)
class SecurityConfig extends GlobalMethodSecurityConfiguration {

    private final DomainAwarePermissionEvaluator permissionEvaluator;
    private final ApplicationContext applicationContext;

    SecurityConfig(@Nonnull DomainAwarePermissionEvaluator permissionEvaluator,
                   @Nonnull ApplicationContext applicationContext) {
        this.permissionEvaluator = permissionEvaluator;
        this.applicationContext = applicationContext;
    }

    /**
     * Создание специального {@link MethodSecurityExpressionHandler}
     * который регистрируется через {@link ExpressionBasedPreInvocationAdvice}.
     * По умолчанию {@link DefaultMethodSecurityExpressionHandler},
     * который опционально иньектирует {@link AuthenticationTrustResolver}.
     */
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        val expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        expressionHandler.setApplicationContext(applicationContext);

        return expressionHandler;
    }

    /**
     * Иерархия ролей из конфигурационного файла
     * @param spe {@link SecurityProperties}
     * @return коллекция {@link GrantedAuthority}
     */
    @Bean
    public RoleHierarchy roleHierarchy(SecurityProperties spe) {
        val roleHierarchy = new RoleHierarchyImpl();
        String roleHierarchyStringRepresentation = String.join(" ", ROLES_ALL);
//        String roleHierarchyStringRepresentation = RoleHierarchyUtils.roleHierarchyFromMap(
//                spe.getRoleHierarchy()
//        );
        roleHierarchy.setHierarchy(
                roleHierarchyStringRepresentation
        );
        return roleHierarchy;
    }
}
