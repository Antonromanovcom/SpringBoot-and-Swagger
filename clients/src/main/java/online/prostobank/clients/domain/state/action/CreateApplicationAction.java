package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.events.ApplicationCreator;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateApplicationAction implements Action<ClientStates, ClientEvents> {
    private final ApplicationEventPublisher publisher;
    @Override
    public void execute(StateContext<ClientStates, ClientEvents> context) {
        Optional.of(context)
                .map(StateContext::getExtendedState)
                .map(ExtendedState::getVariables)
                .map(var -> var.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
                .map(res -> (AccountApplication) res)
                .ifPresent(result -> publisher.publishEvent(new ApplicationCreator(result.getId())));
    }
}
