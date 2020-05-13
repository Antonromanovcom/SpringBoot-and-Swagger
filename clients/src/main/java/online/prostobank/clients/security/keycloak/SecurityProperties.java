package online.prostobank.clients.security.keycloak;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Иерархия ролей, определение списка прав
 */
@Getter
@Setter
@ConfigurationProperties("security.prop")
public class SecurityProperties {

    Map<String, List<String>> roleHierarchy = new LinkedHashMap<>();

    Map<String, List<String>> permissions = new LinkedHashMap<>();
}
