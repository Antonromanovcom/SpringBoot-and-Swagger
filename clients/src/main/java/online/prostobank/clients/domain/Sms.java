package online.prostobank.clients.domain;

import java.io.Serializable;

/**
 * Консолидирует все данные, которые нужны для СМС
 */
public class Sms implements Serializable {

    public final String phoneNumber;
    public final String text;

    public Sms(String phoneNumber, String text) {
        this.phoneNumber = phoneNumber;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("SmsPayload{num='%s', text='%s'}", phoneNumber, text);
    }
}
