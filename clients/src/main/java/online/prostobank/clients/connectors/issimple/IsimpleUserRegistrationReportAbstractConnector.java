package online.prostobank.clients.connectors.issimple;

import club.apibank.connectors.isimple.dto.statement.UserRegistrationReportDto;
import club.apibank.connectors.isimple.util.UserRegistrationReportService;
import online.prostobank.clients.connectors.api.IsimpleAbstractConnector;
import online.prostobank.clients.connectors.exceptions.IsimpleUserRegistrationReportException;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IsimpleUserRegistrationReportAbstractConnector extends IsimpleAbstractConnector {

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IMSIscheduledCheckerAbstractConnector.class);

	public UserRegistrationReportDto retreiveReport(String taxNumber) throws IsimpleUserRegistrationReportException {
		LOG.info("Start retrieving ISimple user report");

		UserRegistrationReportService userRegistrationReportService = new UserRegistrationReportService(new JdbcTemplate(ds()));
		List<UserRegistrationReportDto> userRegistrationReport = userRegistrationReportService.getUserRegistrationReport(taxNumber);
		if (userRegistrationReport.size() != 1) {
			LOG.error("List size from ISimple for tax number {} is {}", taxNumber, userRegistrationReport.size());
			throw new IsimpleUserRegistrationReportException(String.format("List size from ISimple for tax number %s is %d", taxNumber, userRegistrationReport.size()));
		}

		return userRegistrationReport.get(0);
	}
}
