package online.prostobank.clients.security.keycloak;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Распознание прав "по умолчанию"
 */
@Component
@RequiredArgsConstructor
class DefaultPermissionResolver implements PermissionResolver {

    private final SecurityProperties securityProperties;

    /**
     * Распознание прав по роли из {@link Authentication#getAuthorities()}
     * @param authentication {@link Authentication}
     * @return {@code Set<String>} прав
     */
    @Override
    public Set<String> resolve(@Nonnull Authentication authentication) {
        return authentication.getAuthorities().stream()
                .flatMap(this::permissionsForRole)
                .collect(Collectors.toSet());
    }

    /**
     * Получение прав для роли
     * @param authority {@link GrantedAuthority}
     * @return права
     */
    private Stream<String> permissionsForRole(GrantedAuthority authority) {
        return new HashSet<>(
                securityProperties
                        .getPermissions()
                        .getOrDefault(authority.getAuthority(), Collections.emptyList())
        ).stream();
    }

}
