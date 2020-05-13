package online.prostobank.clients.domain.state.action;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfirmedAction implements Action<ClientStates, ClientEvents> {
    @Override
    public void execute(StateContext<ClientStates, ClientEvents> context) {
        log.warn("TODO::implement:selfEmployedApplication");
    }
}
