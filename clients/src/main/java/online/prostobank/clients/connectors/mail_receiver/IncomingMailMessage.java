package online.prostobank.clients.connectors.mail_receiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Data
public class IncomingMailMessage {
    private String id;
    private InternetAddress from;
    private String subject;
    private List<Attachment> files;
    private String contentMsg;

    private javax.mail.Message source;

    public IncomingMailMessage(MimeMessage source, List<Attachment> files, String contentMsg) throws MessagingException {
        this.source = source;

        this.id = source.getMessageID();
        this.from = ((InternetAddress) source.getFrom()[0]);
        this.subject = source.getSubject();
        this.files = files;
        this.contentMsg = Jsoup.parse(contentMsg).text();
    }

    public void setRead() {
        try {
            source.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
           log.warn("Нет возможности выставить флаг SEEN у сообщения... ", e);
        }
    }

    @Data
    @AllArgsConstructor
    static class Attachment {
        private String filename;
        private InputStream inputStream;
        private String mimeType;
    }
}
