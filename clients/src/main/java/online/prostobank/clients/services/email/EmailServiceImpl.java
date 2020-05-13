package online.prostobank.clients.services.email;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.config.EmailServiceConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
	public static final String EXCEL_FILE_TYPE = ".xls";

	private final EmailServiceConfig config;
	private final JavaMailSenderImpl emailSender = new JavaMailSenderImpl();

	public EmailServiceImpl(EmailServiceConfig config) {
		this.config = config;
		emailSender.setHost(config.getHost());
		emailSender.setPort(config.getPort());
		emailSender.setUsername(config.getUsername());
		emailSender.setPassword(config.getPassword());

		Properties props = emailSender.getJavaMailProperties();
		props.putAll(config.getProperties());
	}

	@Override
	public void sendEmail(@Nullable File fileToAttach) {
		sendEmail(fileToAttach, config.getSubject(), config.getMessageText(), config.getDefaultRecipient());
	}

	@Override
	public void sendEmail(@Nullable File fileToAttach,
						  @Nonnull String emailSubject,
						  @Nonnull String messageText,
						  @Nonnull String sentTo) {
		try {
			val mimeMessage = emailSender.createMimeMessage();
			val helper = new MimeMessageHelper(mimeMessage, true);
			helper.setSubject(emailSubject);
			helper.setText(messageText);
			helper.setTo(sentTo);
			helper.setFrom(config.getSentFrom());
			if (fileToAttach != null) {
				helper.addAttachment(fileToAttach.getName(), fileToAttach);
			}
			emailSender.send(mimeMessage);
		} catch (MessagingException e) {
			log.error("Unable to send Email :: {}", ExceptionUtils.getRootCauseMessage(e));
		}
	}

	@Override
	public void sendWorkBook(@Nonnull XSSFWorkbook myExcelBook) {
		sendWorkBook(myExcelBook, config.getSubject(), config.getMessageText(), config.getDefaultRecipient());
	}

	@Override
	public void sendWorkBook(@Nonnull XSSFWorkbook myExcelBook,
							 @Nonnull String emailSubject,
							 @Nonnull String messageText,
							 @Nonnull String sentTo) {
		CompletableFuture.supplyAsync(() -> {
			File tmpFile = null;
			try {
				log.info("Sending email to :: {}", sentTo);
				tmpFile = File.createTempFile("tempExcel", EXCEL_FILE_TYPE);
				val stream = new FileOutputStream(tmpFile);
				myExcelBook.write(stream);
				sendEmail(tmpFile, emailSubject, messageText, sentTo);
				stream.close();
				return true;
			} catch (IOException e) {
				log.error("Unable to create tmp file of attachment :: {}",
						ExceptionUtils.getRootCauseMessage(e)
				);
				return false;
			} finally {
				if (tmpFile != null) {
					tmpFile.delete();
				}
			}
		});
	}
}
