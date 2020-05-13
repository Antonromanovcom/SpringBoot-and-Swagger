package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.ChecksDTOExtended;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ChecksResultValue;
import online.prostobank.clients.domain.enums.FailReason;
import online.prostobank.clients.domain.events.AccountApplicationReservedEvent;
import online.prostobank.clients.domain.events.ChecksDeclined;
import online.prostobank.clients.domain.repository.status_log.StatusHistoryRepository;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import online.prostobank.clients.services.forui.ChecksService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecksAction implements Action<ClientStates, ClientEvents> {
	private final ChecksService checksService;
	private final ApplicationEventPublisher bus;
	private final StatusHistoryRepository statusHistoryRepository;

	@Override
	public void execute(StateContext<ClientStates, ClientEvents> context) {
		log.info("Старт цепочки комплексной проверки клиента");
		Optional<AccountApplication> accountApplication = Optional.ofNullable(context)
				.map(StateContext::getExtendedState)
				.map(ExtendedState::getVariables)
				.map(m -> m.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
				.map(client -> {
					log.info("Проверка клиента начата id = {}, текущий статус {}", ((AccountApplication)client).getId(),
							((AccountApplication)client).getStatus().getValue().name());
					if (((AccountApplication)client).getStatus().getValue().equals(Status.ERR_AUTO_DECLINE)) {
						((AccountApplication) client).setStatus(new StatusValue(Status.CONTACT_INFO_CONFIRMED));
					}
					return (AccountApplication) client;
				})
				.map(checksService::kontur)
				.map(result -> {
					log.info("Результат проверки Контур клиента id = {}, текущий статус {}", result.getSecond().getId(),
							result.getSecond().getStatus().getValue().name());
					return result.getSecond();
				})
				.map(checksService::recheckP550)
				.map(result -> {
					log.info("Результат проверки п550 клиента id = {}, текущий статус {}", result.getSecond().getId(),
							result.getSecond().getStatus().getValue().name());
					return result.getSecond();
				})
				//отключена проверка паспорта в цепочке автоматических проверок
//				.map(checksService::checkPassport)
//				.map(Pair::getSecond)
				.map(checksService::arrestsFns)
				.map( result -> {
					log.info("Результат проверки Аресты ФНС клиента id = {}, текущий статус {}", result.getSecond().getId(),
							result.getSecond().getStatus().getValue().name());
					return result.getSecond();
				});
		log.info("Проверка клиента завершена");
		Status clientStatus = accountApplication
				.map(client -> {
					log.info("Значение статуса старого типа после комплексной проверки clientId = {}, status = {}",
							client.getId(), client.getStatus().getValue().name());
					return client.getStatus();
				})
				.map(StatusValue::getValue)
				.orElse(Status.ERR_AUTO_DECLINE);


		if (clientStatus.equals(Status.ERR_AUTO_DECLINE)) {
			log.info("Результат проверки привел к автоотказу");
			if (context != null) {
				log.info("Текущее состояние SM {}", context.getStateMachine().getState());
				log.info("Сигнал SM на автоотказ");

				//для отдельных типов автоотказа нужно отправлять события вручную
				accountApplication.ifPresent(application -> {
					if (application.getChecks() != null) {
						if (ChecksResultValue.HAVE_ARREST.equals(application.getChecks().getArrestsFns())) {
							bus.publishEvent(new ChecksDeclined(application, FailReason.ARREST));
						} else if (!ChecksResultValue.OK.equals(application.getChecks().getP550check())
								|| !ChecksResultValue.OK.equals(application.getChecks().getP550checkFounder())
								|| !ChecksResultValue.OK.equals(application.getChecks().getP550checkHead())) {
							bus.publishEvent(new ChecksDeclined(application, FailReason.P550));
						}
					}
				});

				accountApplication.ifPresent(client ->
						statusHistoryRepository.insertStatusHistory(
								client.getId(),
								context.getStateMachine().getState().getId(),
								ClientEvents.AUTO_DECLINE,
								null,
								SecurityContextHelper.getCurrentUsername(),
								ChecksDTOExtended.createFrom(client).getChecksAsString())
				);

				context.getStateMachine().sendEvent(ClientEvents.AUTO_DECLINE);
			}
		} else {
			if (accountApplication.isPresent()) {

				AccountApplication application = accountApplication.get();
				if (application.getAccount() == null || StringUtils.isEmpty(application.getAccount().getAccountNumber())) {
					log.info("Send AccountApplicationReservedEvent event");

					Optional<FailReason> failReason = accountApplication
							.map(checksService::reserve)
							.flatMap(Pair::getFirst);

					if (failReason.isPresent()) {
						FailReason reserveFail = failReason.get();
						log.info("Ошибка при резервировании счета для clientId={}", application.getId());
						log.error(reserveFail.name());
						bus.publishEvent(new ChecksDeclined(application, reserveFail));
					} else {
						bus.publishEvent(new AccountApplicationReservedEvent(application.getId(), application.getClient().getPhone()));
						log.info("Сигнал SM CHECKS_DONE для clientId={}", application.getId());

						statusHistoryRepository.insertStatusHistory(
								application.getId(),
								context.getStateMachine().getState().getId(),
								ClientEvents.AUTO_DECLINE,
								null,
								SecurityContextHelper.getCurrentUsername(),
								"Проверки проведены успешно. Счет успешно зарезервирован");

						context.getStateMachine().sendEvent(ClientEvents.CHECKS_DONE);
					}
				} else {
					log.info("Счет для clientId = {} уже был зарезервирован ранее", application.getId());
					log.info("Сигнал SM CHECKS_DONE для clientId = {}", application.getId());

					statusHistoryRepository.insertStatusHistory(
							application.getId(),
							context.getStateMachine().getState().getId(),
							ClientEvents.CHECKS_DONE,
							null,
							SecurityContextHelper.getCurrentUsername(),
							"Проверки проведены успешно. Счет уже был зарезервирован ранее");

					context.getStateMachine().sendEvent(ClientEvents.CHECKS_DONE);
				}
			}
		}
	}
}
