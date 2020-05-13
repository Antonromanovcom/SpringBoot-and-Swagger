package online.prostobank.clients.connectors.mail_receiver;

import com.sun.mail.imap.IMAPNestedMessage;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.InboundEmailConfigProperties;
import online.prostobank.clients.domain.ImapTransactionSynchronizationProcessor;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.integration.support.PropertiesBuilder;
import org.springframework.integration.transaction.DefaultTransactionSynchronizationFactory;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
import org.springframework.integration.transaction.TransactionSynchronizationProcessor;
import org.springframework.messaging.Message;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "inbound-mail.enabled", havingValue = "true")
public class InboundEmailConfig {

    @Bean
    public ImapMailReceiver imapMailReceiver(InboundEmailConfigProperties config) {
        ImapMailReceiver mailReceiver = new ImapMailReceiver(config.getUrl());

        mailReceiver.setSimpleContent(true);
        mailReceiver.setShouldMarkMessagesAsRead(false);
        mailReceiver.setShouldDeleteMessages(false);
        mailReceiver.setMaxFetchSize(1);

        mailReceiver.setJavaMailProperties(
                new PropertiesBuilder()
                        .put("mail.debug", "false")
                        .get());

        return mailReceiver;
    }


    private TransactionSynchronizationFactory mailMovingSyncFactory(InboundEmailConfigProperties config,
                                                                    AccountApplicationRepository accountApplicationRepository,
                                                                    AccountApplicationRepositoryWrapper repositoryWrapper) {
        TransactionSynchronizationProcessor processor =
                new ImapTransactionSynchronizationProcessor(
                        config.getSuccessFolder(),
                        config.getFailedFolder(),
                        accountApplicationRepository,
                        repositoryWrapper
                );

        return new DefaultTransactionSynchronizationFactory(processor);
    }


    @Bean
    public IntegrationFlow imapMailFlow(ImapMailReceiver receiver,
                                        MailAttachmentProcessor service,
                                        InboundEmailConfigProperties config,
                                        AccountApplicationRepository accountApplicationRepository,
                                        AccountApplicationRepositoryWrapper repositoryWrapper
    ) {
        return IntegrationFlows
                .from(
                        Mail.imapInboundAdapter(receiver),
                        c -> c.poller(Pollers
                                .fixedDelay(config.getFixedDelay(), 0)
                                .maxMessagesPerPoll(config.getMaxMessagesPerPoll())
                                .transactional(new PseudoTransactionManager())
                                .transactionSynchronizationFactory(mailMovingSyncFactory(config, accountApplicationRepository, repositoryWrapper))
                                .sendTimeout(config.getTimeout())
                                .receiveTimeout(config.getTimeout())
                        )
                )
                .transform(Message.class, this::decodeMessage)
                .filter(Objects::nonNull)
                .handle(p -> this.processMessage(p, service))
                .get();
    }

    private void processMessage(Message p, MailAttachmentProcessor svc) {
        Object payload = p.getPayload();
        if (!(payload instanceof IncomingMailMessage)) {
            log.warn("payload is not IncomingMailMessage: {}", payload);
            return;
        }
        IncomingMailMessage msg = (IncomingMailMessage) payload;

        log.info("got message from {} with {} attachments", msg.getFrom().toUnicodeString(), msg.getFiles().size());

        svc.handleIncomingMessage(msg);

        msg.setRead();
    }

    private IncomingMailMessage decodeMessage(Message p) {
        Object payload = p.getPayload();
        if (!(payload instanceof MimeMessage)) {
            log.warn("payload in not MimeMessage: {}", payload);
            return null;
        }
        MimeMessage msg = (MimeMessage) payload;
        log.info("Получено сообщение от {}", getFromAddresses(msg));
        try {

            Object content = msg.getContent();
            if (!(content instanceof Multipart)) {
                return new IncomingMailMessage(msg, new ArrayList<>(), content.toString());
            }

            Multipart parts = (Multipart) content;

            StringBuffer bufferContentMsg = new StringBuffer();
            List<IncomingMailMessage.Attachment> attachments = new ArrayList<>(getAttachments(parts.getBodyPart(0)));
            if (attachments.size() > 0) {
                bufferContentMsg.append(getContentMsg(parts.getBodyPart(0)));
            }
            for (int i = 1; i < parts.getCount(); i++) {
                attachments.addAll(getAttachments(parts.getBodyPart(i)));
            }

            return new IncomingMailMessage(msg, attachments, bufferContentMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getContentMsg(BodyPart bodyPart) throws IOException, MessagingException {
        Object content = bodyPart.getContent();
        if (content instanceof String) {
            return content.toString();
        }
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart body = multipart.getBodyPart(i);
                Object bodyContent = body.getContent();
                if (bodyContent instanceof String) {
                    return bodyContent.toString();
                }
            }
        }
        return "";
    }

    private List<IncomingMailMessage.Attachment> getAttachments(BodyPart part) throws Exception {
        List<IncomingMailMessage.Attachment> files = new ArrayList<>();

        Object content = part.getContent();
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                files.addAll(getAttachments(bodyPart));
            }
        } else {
            if (content instanceof IMAPNestedMessage) {
                IncomingMailMessage nestedMessage = decodeMessage((Message)content);
                if (nestedMessage != null) {
                    files.addAll(nestedMessage.getFiles());
                }
            }
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || StringUtils.isNotBlank(part.getFileName())) {
                files.add(new IncomingMailMessage.Attachment(
                        MimeUtility.decodeText(part.getFileName()),
                        part.getInputStream(),
                        part.getContentType()
                ));
            }
        }
        return files;
    }

    private String getFromAddresses(MimeMessage msg) {
        try {
            if (msg == null || msg.getFrom() == null) {
                return "Нет сведений об адресах отправителя";
            }
            return Arrays.stream(msg.getFrom()).map(Address::toString).collect(Collectors.joining());
        } catch (Exception ex) {
            log.error("Не удалось прочитать ардеса отправителей", ex);
            return "Нет сведений об адресах отправителя";
        }
    }
}
