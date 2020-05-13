package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AutoDeclineAction implements Action<ClientStates, ClientEvents> {
	private final ApplicationEventPublisher bus;

	@Override
	public void execute(StateContext<ClientStates, ClientEvents> context) {
		// todo это уже происходит при переходе в Status.ERR_AUTO_DECLINE, но это старая логика tryMove

//		FailReason failReason = Optional.ofNullable(context)
//				.map(StateContext::getExtendedState)
//				.map(ExtendedState::getVariables)
//				.map(m -> m.get(MarkEnum.FAIL_REASON))// todo неготово
//				.map(client -> (FailReason) client).get();
//
//		AccountApplication accountApplication = Optional.ofNullable(context)
//				.map(StateContext::getExtendedState)
//				.map(ExtendedState::getVariables)
//				.map(m -> m.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
//				.map(client -> (AccountApplication) client).get();
//
//		bus.publishEvent(new ChecksDeclined(accountApplication, failReason));
	}
}
