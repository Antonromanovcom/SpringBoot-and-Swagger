package online.prostobank.clients.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.config.properties.JmsEventsProperties;
import online.prostobank.clients.connectors.ExternalConnectors;
import online.prostobank.clients.domain.enums.EventName;
import online.prostobank.clients.domain.events.ClientNeedRegisterInKeycloakFofDbo;
import online.prostobank.clients.domain.exceptions.ClientRegisterInKeycloakException;
import online.prostobank.clients.services.dbo.service.ClientRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.mail.MailSendException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static club.apibank.connectors.KeycloakApiConnectorImpl.OTHER_ERROR_STATUS;

@Slf4j
@RequiredArgsConstructor
@Service
public class JmsEvents {
	private final ExternalConnectors externalConnectors;
	private final ClientRegisterService clientRegisterService;
	private final JmsEventsProperties config;

	private static volatile boolean shouldSendEmail = true;
	private static volatile Instant switchOffSendEmailDate;

	/**
	 * Вызывается на входящее сообщение о том, что надо отправить sms
	 */
	@JmsListener(destination = EventName.SMS_NAME)
	public void listenSms(@Payload Sms m,
						  @Header(JmsHeaders.MESSAGE_ID) String messageID) {
		log.info("Destination: 'sms'. Message to send {}, mid={}", m, messageID);

		val blacklistedSms = config.getBlacklistedSms();
		if (!StringUtils.isEmpty(m.phoneNumber)
				&& !CollectionUtils.isEmpty(blacklistedSms)
				&& !blacklistedSms.contains(m.phoneNumber)) {  // temporary lock this number
			externalConnectors.getSmsSender().send("+7" + ClientValue.normalizePhone(m.phoneNumber), m.text);
		} else {
			log.warn("There is no phone number to send sms '{}' or phone is blocked, mid={}", m, messageID);
			if (!CollectionUtils.isEmpty(blacklistedSms)
					&& !blacklistedSms.contains(m.phoneNumber)) {
				log.warn("Sending sms to spamer. But won't send"); // to debug
			}
		}
	}

	/**
	 * Вызывается на входящее сообщение о том, что надо отправить почту
	 */
	@JmsListener(destination = EventName.EMAIL_NAME/*, containerFactory = "jmsListenerContainerFactory"*/)
	public void listenEmail(@Payload Email m,
							@Header(JmsHeaders.MESSAGE_ID) String messageID) {
		log.info("Destination: 'email'. Message to send {}, mid {}", m, messageID);
		if (!StringUtils.isEmpty(((String) m.obj.get("phone")))) {
			externalConnectors.getEmarsysMailer().send(m);
		} else {
			log.warn("There is no key field (phone number) for Emarsys mail send '{}', mid = {}", m, messageID);
		}
	}

	/**
	 * Вызывается на входящее сообщение о том, что надо отправить триггер
	 */
	@JmsListener(destination = EventName.TRIGGER_NAME)
	public void listenTriggerEmail(@Payload Email m,
								   @Header(JmsHeaders.MESSAGE_ID) String messageID) {
		log.info("Destination: 'email_trigger'. Message to send {}, mid {}", m, messageID);
		if (!StringUtils.isEmpty(((String) m.obj.get("phone")))) {
			externalConnectors.getEmarsysMailer().sendTrigger(m);
		} else {
			log.warn("There is no key field (phone number) for Emarsys mail send '{}', mid = {}", m, messageID);
		}
	}

	/**
	 * Вызывается на входящее сообщение о том, что надо отправить почту
	 */
	@JmsListener(destination = EventName.SYSTEM_EMAIL_NAME)
	public void listenEmail(@Payload SystemEmail m,
							@Header(JmsHeaders.MESSAGE_ID) String messageID) {
		log.info("Destination: 'sysemail'. Message to send {}, mid {}", m, messageID);
		log.info("Prepare to send sysemail. ShouldSendEmail={}, switchOffSendEmailDate={}, time gap in hours={}",
				shouldSendEmail,
				switchOffSendEmailDate,
				(switchOffSendEmailDate == null ? "0" : ChronoUnit.HOURS.between(switchOffSendEmailDate, Instant.now()))
		);
		if (!StringUtils.isEmpty(m.address)) {
			try {
				if ((shouldSendEmail && switchOffSendEmailDate == null)
						|| (ChronoUnit.HOURS.between(switchOffSendEmailDate, Instant.now()) >= 24)) {
					externalConnectors.getSmtpMailer().send(m);
				} else {
					log.warn("Trying to send email, but shouldSendEmail={}, switchOffSendEmailDate={}, time gap in hours={}",
							shouldSendEmail,
							switchOffSendEmailDate,
							(switchOffSendEmailDate == null ? "0" : ChronoUnit.HOURS.between(switchOffSendEmailDate, Instant.now())));
				}
			} catch (MailSendException mse) {
				if (StringUtils.isNotBlank(mse.getMessage()) && mse.getMessage().contains("Message rejected under suspicion of SPAM")) {
					shouldSendEmail = false;
					switchOffSendEmailDate = Instant.now();
				}
			}

		} else {
			log.warn("There is no address to send sysemail '{}', mid={}", m, messageID);
		}
	}

	@JmsListener(destination = EventName.KEYCLOAK_NAME)
	public void listenKeycloak(@Payload ClientNeedRegisterInKeycloakFofDbo event,
							   @Header(JmsHeaders.MESSAGE_ID) String messageID) {
		log.info("Destination: 'keycloak', mid {}", messageID);
		int registerStatus = clientRegisterService.clientRegisterInKeycloak(event);
		if (registerStatus == OTHER_ERROR_STATUS) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e.getLocalizedMessage(), e);
			}
			throw new ClientRegisterInKeycloakException(registerStatus);
		}
	}
}
