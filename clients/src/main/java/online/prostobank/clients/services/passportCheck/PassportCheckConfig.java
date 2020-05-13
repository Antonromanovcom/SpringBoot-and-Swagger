package online.prostobank.clients.services.passportCheck;

import club.apibank.connectors.InnNalogRuConnector;
import club.apibank.connectors.InnNalogRuConnectorImpl;
import club.apibank.connectors.captcha.lab.CaptchaLABCaptchaSolverConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class PassportCheckConfig {
	// todo перенести в конфиги (из других мест тоже) после полного перехода на новый гит-флоу
	private String nalogRuApiHost = "https://service.nalog.ru/inn.do";
	private String captchaVersion = "2";
	private int retryCount = 5;
	private int captchaSolveRetryCount = 5;
	private String captchaLabServiceHost = "http://service-captcha-lab.com";
	private String captchaLabServiceHostAuthKey = "49y9043ls87b26ofjfejpyajvjzwpymt";
	private int solveIntervalSeconds = 3;
	private int retryDelaySeconds = 5;

	@Bean
	public InnNalogRuConnector innNalogRuConnector() {
		return new InnNalogRuConnectorImpl(
				nalogRuApiHost
				, captchaVersion
				, retryCount
				, captchaSolveRetryCount
				, new CaptchaLABCaptchaSolverConnector(
				Duration.ofSeconds(solveIntervalSeconds),
				Duration.ofSeconds(retryDelaySeconds),
				retryCount,
				captchaLabServiceHost,
				captchaLabServiceHostAuthKey
		));
	}
}
