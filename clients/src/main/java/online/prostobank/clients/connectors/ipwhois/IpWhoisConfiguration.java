package online.prostobank.clients.connectors.ipwhois;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ipwhois")
@Data
public class IpWhoisConfiguration {

    @Value("${url:http://free.ipwhois.io/json/}")
    private String baseUrl;
}
