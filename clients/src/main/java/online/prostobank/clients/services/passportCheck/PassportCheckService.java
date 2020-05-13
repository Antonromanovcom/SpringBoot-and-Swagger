package online.prostobank.clients.services.passportCheck;

import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.PersonValue;

public interface PassportCheckService {
	PassportCheckServiceImpl.CheckResult checkPassport(PersonValue person, ClientValue client);
}
