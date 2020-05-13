package online.prostobank.clients.connectors.sms;

import club.apibank.connectors.mfms.MFMSConnector;
import club.apibank.connectors.mfms.MFMSConnectorDTO;
import club.apibank.connectors.mfms.MFMSProperties;
import online.prostobank.clients.config.properties.MfsSenderProperties;
import online.prostobank.clients.connectors.api.SmsSender;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yv
 */
public class MfmsSender implements SmsSender {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MfmsSender.class);

    @Autowired private MfsSenderProperties config;

    public MfmsSender() {

    }

    /**
     * Отправка sms
     * @param number
     * @param text
     * @return
     */
    @Override
    public boolean send(String number, String text) {
        LOG.info("Sending sms to {} with text {}", number, text );
        MFMSConnector.injectConnectionProperties(
                new MFMSProperties(
                        config.getLogin(),
                        config.getPassword(),
                        config.getUri()
                )
        );
        MFMSConnectorDTO dto = MFMSConnector.exchangeSmsRequest(
                number,
                text,
                config.getPartner()
        );
        return dto != null;
    }
}
