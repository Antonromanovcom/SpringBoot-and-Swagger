package online.prostobank.clients.connectors.sms;

import online.prostobank.clients.connectors.api.SmsSender;
import org.slf4j.Logger;

/**
 * Заглушечный высылатор СМС
 * @author yv
 */
public class FakeSmsSender implements SmsSender {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FakeSmsSender.class);
    
    @Override
    public boolean send(String number, String text) {
        LOG.warn("FAKING SMS TO {} WITH TEXT {}", number, text);
        return true;
    }
}
