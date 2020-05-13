package online.prostobank.clients.connectors.abs;

import club.apibank.connectors.kub.AbsConnector;
import club.apibank.connectors.kub.data.dto.request.AccountReservationRequest;
import club.apibank.connectors.kub.data.dto.response.AccountReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.connectors.api.AbsService;
import online.prostobank.clients.connectors.issimple.PartnerType;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.AccountValue;
import online.prostobank.clients.domain.enums.BankId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AbsServiceImpl implements AbsService {
	private final AbsConnector absConnector;

	@Override
	public CheckResult doCheck(@Nonnull String taxNumber) {
		return suspiciousActivityCheck(taxNumber);
	}

	private CheckResult suspiciousActivityCheck(@Nonnull String taxNumber) {
		try {
			return Optional.ofNullable(absConnector.processSuspiciousActivityCheck(taxNumber))
					.filter(suspiciousCheckResult -> !suspiciousCheckResult.isSuspicious)
					.map(AbsServiceImpl::notSuspiciousResult)
					.orElseGet(() -> suspiciousResult(taxNumber));
		} catch (IOException e) {
			log.error("Ошибка проверки контрагента в 550-П :: {}, message :: {}",
					taxNumber, ExceptionUtils.getMessage(e));
			return ioExceptionResult();
		}
	}

	private static CheckResult notSuspiciousResult(AbsConnector.SuspiciousCheckResult suspiciousCheckResult) {
		CheckResult result = new CheckResult();
		result.setIsSuspicious(false);
		return result;
	}

	private CheckResult suspiciousResult(@Nonnull String taxNumber) {
		CheckResult result = new CheckResult(); //todo: make model fields private and inline
		result.setErrorReasons(new String[]{"ИНН " + taxNumber + " не прошел проверку"}); //todo: move to enum
		result.setIsSuspicious(true);
		return result;
	}

	private CheckResult ioExceptionResult() {
		CheckResult result = new CheckResult();
		result.setErrorReasons(new String[]{"Ошибка проверки контрагента в 550-П"}); //todo: move to enum
		return result;
	}

	@Override
	public AccountValue makeReservation(AccountApplication app) throws AbsResponseException {
		try {
			AccountReservationRequest request = new AccountReservationRequest();
			request.setInn(app.getClient().getNumber());
			request.setSource(PartnerType.PROSTO.name());

			AccountReservationResponse response = absConnector.processAccountReservation(request);
			checkAbsResponseAndThrowOnInvalid(response);
			return new AccountValue(response.getAcctNo(), response.getRequestId(), BankId.KUB);
		} catch (Exception ex) {
			log.error("Ошибка неустановленного происхождения при обращении к АБС за резервированием счета", ex);
			throw new AbsResponseException(ex.getMessage(), ex);
		}
	}

	private void checkAbsResponseAndThrowOnInvalid(AccountReservationResponse response) throws AbsResponseException {
		if (response == null) {
			throw new AbsResponseException("ABS response is null");
		}
		if (StringUtils.isEmpty(response.getAcctNo())) {
			throw new AbsResponseException("ABS response accountNumber is empty or null");
		}
		if (StringUtils.isEmpty(response.getAcctNo())) {
			throw new AbsResponseException("ABS response accountNumber is empty or null");
		}
		if (StringUtils.isEmpty(response.getRequestId())) {
			throw new AbsResponseException("ABS response requestId is empty or null");
		}
	}
}
