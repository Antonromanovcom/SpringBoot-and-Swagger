package online.prostobank.clients.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.events.ApplicationChanger;
import online.prostobank.clients.domain.events.EmarsysSaveEditEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AccountApplicationRepositoryWrapperImpl implements AccountApplicationRepositoryWrapper {
	private final AccountApplicationRepository accountApplicationRepository;
	private final ApplicationEventPublisher bus;

	@Override
	public Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication currentApp, boolean toEmarsys) {
		try {
			AccountApplication application = accountApplicationRepository.saveAndFlush(currentApp);
			bus.publishEvent(new ApplicationChanger(application.getId()));
			if (toEmarsys) {
				bus.publishEvent(new EmarsysSaveEditEvent(application));
			}
			return Pair.of(true, application); // сохранено стандартно
		} catch (ObjectOptimisticLockingFailureException exception) {
			log.warn("Optimistic locking for {}. Merging..", currentApp);
			return Pair.of(false, currentApp); // не сохранено
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage(), ex);
			return Pair.of(false, currentApp); // не сохранено
		}
	}

	@Override
	public Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication application) {
		return saveAccountApplication(application, false);
	}
}
