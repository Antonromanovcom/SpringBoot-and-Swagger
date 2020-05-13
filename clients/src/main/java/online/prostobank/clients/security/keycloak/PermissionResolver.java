package online.prostobank.clients.security.keycloak;

import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Общий интерфейс распознавание прав
 */
public interface PermissionResolver {
    /**
     * @param authentication пользователь
     * @return права
     */
    Set<String> resolve(@Nonnull Authentication authentication);
}
