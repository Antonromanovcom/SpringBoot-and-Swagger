package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.ChecksDTOExtended;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.AccountApplicationEntity;
import online.prostobank.clients.domain.enums.FailReason;
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

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Checks550Action implements Action<ClientStates, ClientEvents> {
	private final ChecksService checksService;
	private final ApplicationEventPublisher bus;
	private final StatusHistoryRepository statusHistoryRepository;

	@Override
	public void execute(StateContext<ClientStates, ClientEvents> context) {
		Optional<AccountApplication> accountApplication = Optional.ofNullable(context)
				.map(StateContext::getExtendedState)
				.map(ExtendedState::getVariables)
				.map(m -> m.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
				.map(client -> (AccountApplication) client)
				.map(checksService::recheckP550)
				.map(Pair::getSecond);

		Status clientStatus = accountApplication
				.map(AccountApplicationEntity::getStatus)
				.map(StatusValue::getValue)
				.orElse(Status.ERR_AUTO_DECLINE);

		if (clientStatus.equals(Status.ERR_AUTO_DECLINE)) {
			if (context != null) {
				accountApplication.ifPresent(client -> {
							log.info("Результат повторной проверки п550 для clientId = {} привел к автоотказу", client.getId());
							log.info("Текущее состояние SM для clientId = {} {}", client.getId(), context.getStateMachine().getState());
							log.info("Сигнал SM на автоотказ для clientId = {}", client.getId());
							//для отдельных типов автоотказа нужно отправлять события вручную
							bus.publishEvent(new ChecksDeclined(client, FailReason.P550));

							statusHistoryRepository.insertStatusHistory(
									client.getId(),
									context.getStateMachine().getState().getId(),
									ClientEvents.AUTO_DECLINE,
									null,
									SecurityContextHelper.getCurrentUsername(),
									ChecksDTOExtended.createFrom(client).getChecksAsString());
						}
				);
				context.getStateMachine().sendEvent(ClientEvents.AUTO_DECLINE);
			}
		}
	}
}
