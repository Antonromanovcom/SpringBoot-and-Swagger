package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.avro.dbo.AccountStatus;
import online.prostobank.clients.api.dto.avro.dbo.ChangeAccountStatus;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.utils.avro.AvroProvider;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static online.prostobank.clients.domain.enums.EventName.DBO_ACCOUNT_CHANGE_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class DboAccountChangeStatusAction implements Action<ClientStates, ClientEvents> {
	private final JmsTemplate sender;
	private final AvroProvider avroProvider;

	@Override
	public void execute(StateContext<ClientStates, ClientEvents> context) {
		Optional.of(context)
				.map(StateContext::getExtendedState)
				.map(ExtendedState::getVariables)
				.map(it -> it.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
				.map(res -> (AccountApplication) res)
				.ifPresent(application -> {
					ChangeAccountStatus changeAccountStatus = new ChangeAccountStatus();
					changeAccountStatus.setUserId(application.getId().toString());
					changeAccountStatus.setInn(application.getClient().getInn());
					changeAccountStatus.setTimeEvent(LocalDate.now());
					changeAccountStatus.setAccountNumber(application.getAccount().getAccountNumber());
					ClientStates clientState = context.getTarget().getId();
					AccountStatus accountStatus = getAccountStatus(clientState);
					if (accountStatus == null) {
						log.error("DboAccountChangeStatusAction, clientState: {}", clientState);
						return;
					}
					changeAccountStatus.setStatus(accountStatus);
					byte[] serialize = avroProvider.serialize(changeAccountStatus);

					log.info("sent to {} message: {}", DBO_ACCOUNT_CHANGE_STATUS, changeAccountStatus);
					sender.convertAndSend(DBO_ACCOUNT_CHANGE_STATUS, serialize);
				});
	}

	private AccountStatus getAccountStatus(ClientStates clientState) {
		switch (clientState) {
			case WAIT_FOR_DOCS:
				return AccountStatus.wait_for_docs;
			case ACTIVE_CLIENT:
				return AccountStatus.open;
			case INACTIVE_CLIENT:
				return AccountStatus.closure;
			default:
				return null;
		}
	}
}
