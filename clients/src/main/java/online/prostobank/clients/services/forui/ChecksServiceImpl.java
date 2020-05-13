package online.prostobank.clients.services.forui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.connectors.ArrestsCheckService;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.FailReason;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.services.passportCheck.PassportCheckService;
import online.prostobank.clients.services.passportCheck.PassportCheckServiceImpl;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChecksServiceImpl implements ChecksService {
	private final ArrestsCheckService arrestsCheckService;
	private final PassportCheckService passportCheckService;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;

	@Override
	public Pair<Optional<Exception>, AccountApplication> kontur(AccountApplication currentApp) {
		Exception exceptionLogic = null;
		try {
			currentApp.clientInfoAndUpdate();
			currentApp.checkKonturAndProcess();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			exceptionLogic = e;
		}
		Pair<Boolean, AccountApplication> pair = saveAccountApplication(currentApp);
		return Pair.of(Optional.ofNullable(exceptionLogic), pair.getSecond());
	}

	@Override
	public Pair<Boolean, AccountApplication> arrestsFns(AccountApplication currentApp) {
		currentApp.clientInfoAndUpdate();
		arrestsCheckService.checkArrests(currentApp);
		return saveAccountApplication(currentApp);
	}

	@Override
	public Pair<Optional<Exception>, AccountApplication> recheckP550(AccountApplication currentApp) {
		Exception exceptionLogic = null;
		try {
			currentApp.recheckP550();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			exceptionLogic = e;
		}
		Pair<Boolean, AccountApplication> pair = saveAccountApplication(currentApp);
		return Pair.of(Optional.ofNullable(exceptionLogic), pair.getSecond());
	}

	@Override
	public Pair<Boolean, AccountApplication> checkPassport(AccountApplication currentApp) {
		PassportCheckServiceImpl.CheckResult result = passportCheckService
				.checkPassport(currentApp.getPerson(), currentApp.getClient());
		currentApp.getChecks().setPassportCheck(result.getResult());
		return saveAccountApplication(currentApp);
	}

	@Override
	public Pair<Optional<FailReason>, AccountApplication> reserve(AccountApplication currentApp) {
		FailReason failReason = currentApp.makeReservation();
		Pair<Boolean, AccountApplication> pair = saveAccountApplication(currentApp);
		return Pair.of(Optional.ofNullable(failReason), pair.getSecond());
	}

	private Pair<Boolean, AccountApplication> saveAccountApplication(AccountApplication currentApp) {
		return repositoryWrapper.saveAccountApplication(currentApp);
	}
}
