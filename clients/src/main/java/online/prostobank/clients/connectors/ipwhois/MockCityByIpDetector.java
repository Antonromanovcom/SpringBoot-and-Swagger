package online.prostobank.clients.connectors.ipwhois;

import online.prostobank.clients.connectors.api.CityByIpDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@ConditionalOnMissingBean(IpWhoisConnector.class)
public class MockCityByIpDetector implements CityByIpDetector {
    @Override
    public Optional<String> getCityByIpOrDefault(String ip) {
        return Optional.empty();
    }
}
