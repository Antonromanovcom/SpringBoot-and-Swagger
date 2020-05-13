package online.prostobank.clients.domain;

import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.transaction.IntegrationResourceHolder;
import org.springframework.integration.transaction.TransactionSynchronizationProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImapTransactionSynchronizationProcessor implements TransactionSynchronizationProcessor {

    private final static Pattern PATTERN = Pattern.compile("<(.{0,}?)>");
    private String successFolder;
    private String failedFolder;
    private AccountApplicationRepository accountApplicationRepository;
    private AccountApplicationRepositoryWrapper repositoryWrapper;
    private static final Logger LOG = LoggerFactory.getLogger(ImapTransactionSynchronizationProcessor.class);

    public ImapTransactionSynchronizationProcessor(String successFolder, String failedFolder,
                                                   AccountApplicationRepository accountApplicationRepository,
                                                   AccountApplicationRepositoryWrapper repositoryWrapper) {
        this.successFolder = successFolder;
        this.failedFolder = failedFolder;
        this.accountApplicationRepository = accountApplicationRepository;
        this.repositoryWrapper = repositoryWrapper;
    }

    @Override
    public void processBeforeCommit(IntegrationResourceHolder holder) { }

    @Override
    public void processAfterCommit(IntegrationResourceHolder holder) {
        if(null == holder.getMessage())
            return;
        LOG.info("processed {}", holder.getMessage());
        MimeMessage msg = getMessage(holder);

        moveMessage(msg, successFolder, true);
    }

    @Override
    public void processAfterRollback(IntegrationResourceHolder holder) {
        try {
            LOG.info("throw error with {}", holder.getMessage());
            MimeMessage msg = getMessage(holder);

            addNotificationAboutError(msg);
            moveMessage(msg, failedFolder, false);

            LOG.warn("failed to process message from {}", safeGetFullEmailSenderInfo(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addNotificationAboutError(MimeMessage msg) {
            String emailFrom = null;

            String fullEmailSenderInfo = safeGetFullEmailSenderInfo(msg);

            Matcher m = PATTERN.matcher(fullEmailSenderInfo);
            if (m.find()) {
                emailFrom = m.group(1);
            }

            if(Strings.isNotBlank(emailFrom)) {
                List<AccountApplication> accountApplications = accountApplicationRepository.findByStatusAndEmail(Arrays.asList(
                        ClientStates.NEW_CLIENT,
                        ClientStates.CONTACT_INFO_CONFIRMED,
                        ClientStates.NO_ANSWER,
                        ClientStates.CHECK_LEAD,
                        ClientStates.WAIT_FOR_DOCS,
                        ClientStates.DOCUMENTS_EXISTS,
                        ClientStates.REQUIRED_DOCS
                ), emailFrom);

                if (null != accountApplications) {
                    accountApplications.forEach(a -> {
                        a.addHistoryRecord("Ошибка при обработке email сообщения (добавление документов) ");
                        repositoryWrapper.saveAccountApplication(a);
                    });
                }
            }
    }

    private void moveMessage(MimeMessage msg, String dstFolder, boolean setSeen) {
        try(
                Folder src = openFolder(msg);
                Folder dst = openSiblingFolder(src, dstFolder)
        ) {
            javax.mail.Message[] messages = src.getMessages();
            FetchProfile contentsProfile = new FetchProfile();
            contentsProfile.add(FetchProfile.Item.ENVELOPE);
            contentsProfile.add(FetchProfile.Item.CONTENT_INFO);
            contentsProfile.add(FetchProfile.Item.FLAGS);
            src.fetch(messages, contentsProfile);
//                    LOG.info("message count: {}", messages.length);
            // find this message and mark for deletion
            for (javax.mail.Message message : messages) {
                if (((MimeMessage) message).getMessageID().equals(msg.getMessageID())) {
                    message.setFlag(Flags.Flag.SEEN, setSeen);
                    break;
                }
            }
            dst.appendMessages(new javax.mail.Message[]{msg});
        } catch (Exception e) {
            addNotificationAboutError(msg);
            e.printStackTrace();
        }
    }

    private Folder openFolder(MimeMessage msg) throws MessagingException {
        Folder f = msg.getFolder();
        f.open(Folder.READ_WRITE);
        return f;
    }

    private Folder openSiblingFolder(Folder src, String dstFolder) throws MessagingException {
        Folder sibling = src.getStore().getFolder(dstFolder);
        sibling.open(Folder.READ_WRITE);
        return sibling;
    }

    MimeMessage getMessage(IntegrationResourceHolder h) {
        return (MimeMessage) h.getMessage().getPayload();
    }

    String safeGetFullEmailSenderInfo(MimeMessage msg) {
        try {
            return ((InternetAddress) msg.getFrom()[0]).toUnicodeString();
        } catch (Exception e) {
            addNotificationAboutError(msg);
            LOG.warn("failed to get FROM of message", e);
            return "";
        }
    }

}
