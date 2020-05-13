package online.prostobank.clients.domain.state.guard;

import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfirmedGuard implements Guard<ClientStates, ClientEvents> {
    @Override
    public boolean evaluate(StateContext<ClientStates, ClientEvents> context) {
        return Optional.ofNullable(context)
                .map(StateContext::getExtendedState)
                .map(ExtendedState::getVariables)
                .map(m -> m.get(MarkEnum.INN_MARK))
                .map(Object::toString)
                .map(Boolean::valueOf)
                .orElse(false);
    }
}
