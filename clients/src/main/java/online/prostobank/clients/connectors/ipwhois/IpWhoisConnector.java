package online.prostobank.clients.connectors.ipwhois;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.connectors.api.CityByIpDetector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(
        value="ipwhois.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class IpWhoisConnector implements CityByIpDetector {

    private final RestTemplate restTemplate;
    private final IpWhoisConfiguration config;
    private final IpWhoisLang defaultLang = IpWhoisLang.RU;

    private final String ATTEMPT = "Пытаюсь получить имя города для ip '%s' и url '%s'";
    private final String ERROR = "Ошибка '%s' для ip '%s' и url '%s'";
    private final String SUCCESS = "Город '%s' успешно определен для ip '%s' и url '%s'";
    private final String UNKNOWN_ERROR = "Неизвестная ошибка";

    public IpWhoisConnector(RestTemplate restTemplate,
                            IpWhoisConfiguration config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public Optional<String> getCityByIpOrDefault(String ip) {
        String url = String.format("%s%s?lang=%s", config.getBaseUrl(), ip, defaultLang.getLang());
        log.debug(String.format(ATTEMPT, ip, url));

        try {
            IpWhoisDTO result = restTemplate.getForObject(url, IpWhoisDTO.class);

            if(result == null) {
                log.error(String.format(ERROR, UNKNOWN_ERROR, ip, url));
                return Optional.empty();
            }

            if (!result.isSuccess() || StringUtils.isBlank(result.getCity())) {
                log.warn(String.format(ERROR, result.getMessage(), ip, url));
                return Optional.empty();
            }

            log.info(String.format(SUCCESS, result.getCity(), ip, url));
            return Optional.of(result.getCity());
        }
        catch (RestClientException e) {
            log.error(String.format(ERROR, e.getMessage(), ip, url));
            return Optional.empty();
        }
    }
}
