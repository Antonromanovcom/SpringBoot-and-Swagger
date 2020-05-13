package online.prostobank.clients.connectors.email;

import online.prostobank.clients.connectors.api.EmailConnector;
import online.prostobank.clients.domain.Email;
import online.prostobank.clients.domain.SystemEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Простой рассыльщик email
 *
 * @author yv
 */
public class SmtpEmailConnector implements EmailConnector {

	@Autowired
	private MailSender emailSender;

	@Autowired
	private TemplateEngine tt;

	private final String senderName;

	@Autowired
	public SmtpEmailConnector(String senderName) {
		this.senderName = senderName;
	}

	@Override
	public void send(Email m) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(m.email);
		message.setFrom(senderName);

		message.setSubject(m.id.getTheme());
		Context context = new Context();
		context.setVariables(m.obj);
		message.setText(tt.process(m.id.getTheme() + ".email", context));
		emailSender.send(message);
	}

	@Override
	public void send(SystemEmail m) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(m.address);
		message.setFrom(senderName);
		message.setSubject(m.subject);
		message.setText(m.body);

		emailSender.send(message);
	}
}
