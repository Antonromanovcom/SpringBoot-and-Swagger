package online.prostobank.clients.domain.state.listener;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 * Логирование транзита статусов {@link ClientStates}
 */
@Slf4j
public class ClientStateMachineListener extends StateMachineListenerAdapter<ClientStates, ClientEvents> {
    @Override
    public void stateChanged(State from, State to) {
        log.info(
                String.format(
                        ">>> Transition from %s to %s%n",
                        from == null
                                ? "none"
                                : from.getId(),
                        to.getId()
                )
        );
    }
}
