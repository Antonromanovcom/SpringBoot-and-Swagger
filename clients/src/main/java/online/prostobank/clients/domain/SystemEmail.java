package online.prostobank.clients.domain;

import java.io.Serializable;

public class SystemEmail implements Serializable {

    public final String address;
    public final String subject;
    public final String body;

    /**
     * @param address
     * @param subject
     * @param body
     */
    public SystemEmail(String address, String subject, String body) {
        this.address = address;
        this.subject = subject;
        this.body = body;
    }

    @Override
    public String toString() {
        return "SystemEmail{" +
                "address='" + address + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
