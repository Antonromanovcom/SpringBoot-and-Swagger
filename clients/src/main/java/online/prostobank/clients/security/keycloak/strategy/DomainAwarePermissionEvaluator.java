package online.prostobank.clients.security.keycloak.strategy;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.security.keycloak.PermissionResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Стратегия определения прав для целевого доменного объекта
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainAwarePermissionEvaluator implements PermissionEvaluator {

    private final PermissionResolver permissionResolver;

    /**
     * Определение доменных прав для целевого объекта
     * @param authentication пользователь
     * @param targetDomainObject целевой доменный обхект
     * @param permission права объекта
     * @return {@code true} если права имеются, иначе {@code false}
     */
    @Override
    public boolean hasPermission(
                                @Nonnull  Authentication authentication,
                                @Nullable Object         targetDomainObject,
                                @Nonnull  Object         permission
    ) {
        log.info(
                "Checking permissions :: '{}' \n" +
                    "   for user :: '{}' \n " +
                    "   for target ::'{}'",
                permission,
                authentication.getName(),
                targetDomainObject
        );

        Set<String> hasPermissions = permissionResolver.resolve(authentication);
        Set<String> requiredPermissions = toPermissions(permission);

        val isPermissionsMatch = hasPermissions.containsAll(requiredPermissions);

        if (!isPermissionsMatch) {
            log.debug(
                    "Insufficient permissions:\n" +
                    "Required: {}\n" +
                    "Current: {}",
                    requiredPermissions,
                    hasPermissions
            );
            return false;
        }

        // todo: опционально реализовать делегирование контекста определения прав
        /*
        if ("ROLE_ADMIN_PERMISSION".equals(permission)) {
                return hasRole("ROLE_ADMIN", authentication);
        }
        */

        return true;
    }

    /**
     * Определение прав при отсутствии объекта через его идентификатор
     * @param authentication пользователь
     * @param targetId идентификатор пользователя
     * @param targetType тип объекта
     * @param permission объект прав
     * @return {@code true} если права имеются, иначе {@code false}
     */
    @Override
    public boolean hasPermission(@Nonnull Authentication authentication,
                                 @Nonnull Serializable targetId,
                                 @Nonnull String targetType,
                                 @Nonnull Object permission
    ) {
        return hasPermission(
                authentication,
                new DomainObject(targetId, targetType),
                permission
        );
    }

    private Set<String> toPermissions(@Nonnull Object permission) {
        return permission instanceof String
                ? Collections.singleton((String) permission)
                : Collections.emptySet();
    }

    private boolean hasRole(@Nonnull String role,
                            @Nullable Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = Optional.ofNullable(auth)
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptySet());

        val noPrincipal = !Optional.ofNullable(auth)
                .map(Authentication::getPrincipal)
                .isPresent();
        val noAuthorities = CollectionUtils.isEmpty(authorities);


        if (noAuthorities || noPrincipal) {
            return false;
        }

        return authorities.stream()
                .anyMatch(ga ->
                        role.equals(ga.getAuthority())
                );
    }

    @Value
    static class DomainObject {
        private final Serializable targetId;
        private final String targetType;
    }
}
