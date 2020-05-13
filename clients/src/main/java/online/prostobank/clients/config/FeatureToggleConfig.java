package online.prostobank.clients.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Выключатели фич
 */
@Component
@ConfigurationProperties("toggle")
@NoArgsConstructor
@Getter
@Setter
public class FeatureToggleConfig {
    private List<ToggleValue> features = new ArrayList<>();

    public boolean isFeatureEnabled(String name) {
        return features.stream()
                .filter(it -> it.isName(name))
                .findFirst()
                .orElse(ToggleValue.empty())
                .getEnabled();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ToggleValue {
        private String name = "";
        private Boolean enabled = false;

        public static ToggleValue empty() {
            return new ToggleValue("", false);
        }

        public boolean isName(String name) {
            return this.name != null && this.name.equals(name);
        }
    }
}
