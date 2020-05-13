package online.prostobank.clients.security.keycloak;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import javax.annotation.Nonnull;
import java.util.Collection;

public class RoleResolvingGrantedAuthoritiesMapper extends RoleHierarchyAuthoritiesMapper {

    private final GrantedAuthoritiesMapper delegate;

    public RoleResolvingGrantedAuthoritiesMapper(@Nonnull RoleHierarchy roleHierarchy,
                                                 @Nonnull GrantedAuthoritiesMapper delegate) {
        super(roleHierarchy);
        this.delegate = delegate;
    }

    /**
     * Опциональное преобразование {@link GrantedAuthority}
     * через делегата и распознавание иерархических ролей
     * @param authorities {@link GrantedAuthority}
     * @return {@link GrantedAuthority}
     */
    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            @Nonnull Collection<? extends GrantedAuthority> authorities
    ) {
        Collection<? extends GrantedAuthority> transformedAuthorities =
                delegate.mapAuthorities(authorities);

        Collection<? extends GrantedAuthority> expanededAuthorities =
                super.mapAuthorities(transformedAuthorities);

        return expanededAuthorities;
    }
}

