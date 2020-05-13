package online.prostobank.clients.services.passportCheck;

import club.apibank.connectors.dto.GetInnInfo;
import club.apibank.connectors.exceptions.NalogRuInnNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.connectors.ExternalConnectors;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.connectors.api.KonturService;
import online.prostobank.clients.domain.PersonValue;
import online.prostobank.clients.domain.repository.ExpiredPassportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
public class PassportCheckServiceImpl implements PassportCheckService {
	private static final String PASSPORT_DOC_TYPE = "21";

	@Autowired private ExternalConnectors externalConnectors;

	private final ExpiredPassportsRepository expiredPassportsRepository;

	public PassportCheckServiceImpl(ExpiredPassportsRepository expiredPassportsRepository) {
		this.expiredPassportsRepository = expiredPassportsRepository;
	}

	@Override
	public CheckResult checkPassport(PersonValue person, ClientValue client) {
		log.info("Проверка паспортных данных началась");
		try {
			if (!isAllPassportInfoSet(person, client)) {
				return CheckResult.PASSPORT_INFO_NOT_SET;
			}

			if (!isPassportValid(person.getSer(), person.getNum())) {
				return CheckResult.EXPIRED_PASSPORT;
			}

			String inn = externalConnectors.getInnNalogRuConnector().getInn(getInnNalogRu(client, person));
			KonturService.InfoResult info = externalConnectors.getEgripService().getInfo(client.getInn());

			if (!isKonturHeadTaxNumberFilled(info)) {
				return CheckResult.NO_INN;
			}

			if (!isHeadInnEqualsCompanyInn(info.headTaxNumber, inn)) {
				return CheckResult.PASSPORTS_NOT_EQUAL;
			}

			return CheckResult.OK;
		} catch (NalogRuInnNotFoundException e) {
			log.info("Получен пустой ИНН от сервиса nalog.ru");
			return CheckResult.NO_INN;
		} catch (Exception e) {
			log.error(e.getMessage());
			return CheckResult.UNKNOWN_ERROR;
		}
		finally {
			log.info("Проверка паспортных данных завершена");
		}
	}

	private boolean isPassportValid(String series, String number) {
		return !expiredPassportsRepository.exists(series, number);
	}

	private boolean isAllPassportInfoSet(PersonValue person, ClientValue client) {
		if(client == null || person == null) {
			return false;
		}

		return !isEmpty(client.getSurname()) &&
				!isEmpty(client.getFirstName()) &&
				!isEmpty(client.getSecondName()) &&
				!isEmpty(person.getNum()) &&
				!isEmpty(person.getSer()) &&
				person.getDob() != null;
	}

	private boolean isKonturHeadTaxNumberFilled(KonturService.InfoResult info){
		return info != null && info.headTaxNumber != null;
	}

	private boolean isHeadInnEqualsCompanyInn(String headTaxNumber, String companyInn) {
		return headTaxNumber.equals(companyInn);
	}

	private GetInnInfo getInnNalogRu(ClientValue client, PersonValue person) {
		String surname = client.getSurname();
		String firstName = client.getFirstName();
		String secondName = client.getSecondName();
		String passportNumber = person.getNum();
		String passportSeries = person.getSer();
		LocalDate birthDate = person.getDob();

		GetInnInfo getInnInfo = new GetInnInfo();
		getInnInfo.setSurname(surname);
		getInnInfo.setName(firstName);
		getInnInfo.setPatronymic(secondName);
		getInnInfo.setIdentityDocType(PASSPORT_DOC_TYPE);
		getInnInfo.setIdentityDocNumber(passportSeries + passportNumber);
		getInnInfo.setBirthDate(birthDate);
		return getInnInfo;
	}

	public enum CheckResult {
		EXPIRED_PASSPORT("Паспорт недействителен"),
		PASSPORT_INFO_NOT_SET("Паспортные данные не указаны"),
		NO_INN("Нет ИНН"),
		PASSPORTS_NOT_EQUAL("Неверные паспортные данные"),
		NO_FNS_SERVICE_RESPONSE("Получен пустой ИНН от сервиса nalog.ru"),
		UNKNOWN_ERROR("Ошибка проверки паспортных данных"),
		OK("OK");

		private String result;

		CheckResult(String result) {
			this.result = result;
		}

		public String getResult(){
			return result;
		}
	}
}
