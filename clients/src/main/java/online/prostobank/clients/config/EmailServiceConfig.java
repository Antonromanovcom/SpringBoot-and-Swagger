package online.prostobank.clients.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Data
@Configuration
@PropertySource(value = "classpath:email.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "mail")
public class EmailServiceConfig {
	private String host;
	private Integer port;
	private String username;
	private String password;

    private String defaultRecipient;
	private String sentFrom;
    private String messageText;
	private String subject;

	private Properties properties;
}
