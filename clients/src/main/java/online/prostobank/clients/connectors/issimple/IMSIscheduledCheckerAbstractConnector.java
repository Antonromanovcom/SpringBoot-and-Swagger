package online.prostobank.clients.connectors.issimple;


import club.apibank.connectors.isimple.dto.statement.SuspiciousIMSIDto;
import club.apibank.connectors.isimple.util.IMSICheckerService;
import online.prostobank.clients.connectors.api.IsimpleAbstractConnector;
import online.prostobank.clients.domain.ImsiNotification;
import online.prostobank.clients.domain.ImsiNotificationPhoneNumberOwner;
import online.prostobank.clients.domain.events.IMSINotificationEvent;
import online.prostobank.clients.domain.repository.ImsiNotificationPhoneNumberOwnerRepository;
import online.prostobank.clients.domain.repository.ImsiNotificationRepository;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IMSIscheduledCheckerAbstractConnector extends IsimpleAbstractConnector {

	@Autowired
	private ImsiNotificationPhoneNumberOwnerRepository numberRepository;

	@Autowired
	private ImsiNotificationRepository imsiNotificationRepository;

	@Autowired
	private ApplicationEventPublisher bus;

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IMSIscheduledCheckerAbstractConnector.class);

	@Value("#{'${simple.imsi.notification.users}'.split(',')}")
	private List<String> emailAddresses;

	/**
	 * Checking for changing sim card
	 */
	@Scheduled(fixedRateString = "${simple.imsi.check.period.of.time}")
	public void checkIMSI() {
		LOG.info("Start checking imsi");

		IMSICheckerService imsiCheckerService = new IMSICheckerService(new JdbcTemplate(ds()));
		List<SuspiciousIMSIDto> imsis = imsiCheckerService.getIMSIs(); // retrieve data from isimple
		LOG.info("Found {} suspicious IMSI", imsis.size());

		if (imsis.size() > 0) {
			ImsiNotification imsiNotification = new ImsiNotification();
			imsiNotification.setCreated(Instant.now());

			Set<ImsiNotificationPhoneNumberOwner> imsiNotificationPhoneNumberOwners = new HashSet<>();
			for (SuspiciousIMSIDto imsi : imsis) {
				ImsiNotificationPhoneNumberOwner byPhoneNumberAndOldImsiAndCurrentImsi =
						numberRepository.findByPhoneNumberAndOldImsiAndCurrentImsi(imsi.getPhone(), imsi.getLasGoodIMSI(), imsi.getCurrentIMSI());
				if (byPhoneNumberAndOldImsiAndCurrentImsi == null) {

					ImsiNotificationPhoneNumberOwner imsiNotificationPhoneNumberOwner =
							new ImsiNotificationPhoneNumberOwner(imsi.getPhone(), imsi.getLasGoodIMSI(), imsi.getCurrentIMSI(), imsi.getTaxNumber(), imsi.getFullName());

					imsiNotificationPhoneNumberOwners.add(imsiNotificationPhoneNumberOwner);
				}
			}

			if (!CollectionUtils.isEmpty(imsiNotificationPhoneNumberOwners)) {
				LOG.info("Addresses to send from properties {}", String.join(", ", emailAddresses));
				// if there is no email in properties file
				Set<String> emailSet = emailAddresses.stream().filter(s -> Strings.isNotBlank(s)).collect(Collectors.toSet());

				imsiNotification.setImsiNotificationRecipientUser(emailSet);
				imsiNotification.setImsiNotificationPhoneNumberOwners(imsiNotificationPhoneNumberOwners);

				imsiNotificationRepository.saveAndFlush(imsiNotification);

				bus.publishEvent(new IMSINotificationEvent(imsiNotification)); // maybe send id only
			}
		}
	}
}
