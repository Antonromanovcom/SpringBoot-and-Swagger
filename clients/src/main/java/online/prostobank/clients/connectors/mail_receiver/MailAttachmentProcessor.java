package online.prostobank.clients.connectors.mail_receiver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.events.ApplicationAttachmentSuccessEvent;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.EmailMessagesRepository;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.services.StorageException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailAttachmentProcessor {
    private final AccountApplicationRepository accountApplicationRepository;
    private final AccountApplicationRepositoryWrapper repositoryWrapper;
    private final ApplicationEventPublisher bus;
    private final EmailMessagesRepository emailMessagesRepository;

    public void handleIncomingMessage(IncomingMailMessage msg) {
        List<ClientStates> ss = Arrays.asList(
                ClientStates.NEW_CLIENT,
                ClientStates.CONTACT_INFO_CONFIRMED,
                ClientStates.NO_ANSWER,
                ClientStates.CHECK_LEAD,
                ClientStates.WAIT_FOR_DOCS,
                ClientStates.DOCUMENTS_EXISTS,
                ClientStates.REQUIRED_DOCS
        );

        List<AccountApplication> accountApplications;
        String email = msg.getFrom().getAddress();
        String title = Optional.ofNullable(msg.getSubject())
                .orElse("");

        String accountNumber = "";
        Pattern accountNumberPattern = Pattern.compile("([0-9])\\w+");
        Matcher m = accountNumberPattern.matcher(title);
        if (m.find()) {
            accountNumber = m.group();
        }

        if(Strings.isNotBlank(accountNumber)){
            accountApplications = accountApplicationRepository.findByStatusAndEmailOrAccountNumber(ss, email, accountNumber);
        }else {
            accountApplications = accountApplicationRepository.findByStatusAndEmail(ss, email);
        }

        if (null == accountApplications || accountApplications.isEmpty()) {
            //TODO: Сделать отправку сообщения менеджеру
            log.warn("got mail message with attachments, but no application found");
            return;
        }

        for (IncomingMailMessage.Attachment a : msg.getFiles()) {
            try(InputStream src = a.getInputStream()) {
                byte[] contents = IOUtils.toByteArray(src);
                for (AccountApplication accountApplication : accountApplications) {
                    accountApplication.addAttachmentUnique(a.getFilename(), contents, a.getMimeType());
                    log.info("added {} attachment to Application#{}", a.getFilename(), accountApplication);
                }
            } catch (IOException | StorageException e) {
                log.error("не удалось сохранить вложение", e);
            }
        }
        try {
            if(!StringUtils.isBlank(msg.getContentMsg())){
                for (AccountApplication accountApplication : accountApplications) {
                    emailMessagesRepository.insert(accountApplication, msg);
                }
            }
        } catch (MessagingException e) {
            log.warn("Сообщение email не сохранено", e);
        }

        accountApplications.forEach(accountApplication -> {
            repositoryWrapper.saveAccountApplication(accountApplication);
            bus.publishEvent(new ApplicationAttachmentSuccessEvent(accountApplication));
        });
    }
}
