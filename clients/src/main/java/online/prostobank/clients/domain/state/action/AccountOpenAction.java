package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.services.dbo.service.ClientRegisterService;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountOpenAction implements Action<ClientStates, ClientEvents> {
	private final ClientRegisterService clientRegisterService;

	@Override
	public void execute(StateContext<ClientStates, ClientEvents> context) {
		Optional.ofNullable(context)
				.map(StateContext::getExtendedState)
				.map(ExtendedState::getVariables)
				.map(m -> m.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
				.map(client -> (AccountApplication) client)
				.ifPresent(application -> {
					log.info("clientChangeRoleInKeycloak");
					clientRegisterService.clientChangeRoleInKeycloak(application.getId());
				});
	}
}
