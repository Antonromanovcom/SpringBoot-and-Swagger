package online.prostobank.clients.connectors;

import club.apibank.connectors.InnRosstatConnector;
import club.apibank.connectors.InnRosstatConnectorImpl;
import club.apibank.connectors.captcha.lab.CaptchaLABCaptchaSolverConnector;
import online.prostobank.clients.connectors.api.RosstatService;

import java.time.Duration;

public class RosstatServiceImpl implements RosstatService {

    private InnRosstatConnector innRosstatConnector;

    private final String capthaLabServiceHost = "http://service-captcha-lab.com";
    private final String capthcaLabAuthKey = "49y9043ls87b26ofjfejpyajvjzwpymt";
    private final int captchaRetryCount = 5;
    private int retryCount = 4;
    private final String urlRequest = "http://www.gks.ru/accounting_report";
    private String baseUrl = "http://www.gks.ru";

    public RosstatServiceImpl() {
        this.innRosstatConnector = new InnRosstatConnectorImpl(new CaptchaLABCaptchaSolverConnector(
                    Duration.ofSeconds(3),
                    Duration.ofSeconds(5),
                    retryCount,
                    capthaLabServiceHost,
                    capthcaLabAuthKey),
                captchaRetryCount, retryCount, urlRequest, baseUrl);

    }

    @Override
    public String getData(String inn) throws Exception {
        return innRosstatConnector.getData(inn);
    }
}
