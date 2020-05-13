package online.prostobank.clients.domain.state.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.events.ApplicationIsNewEvent;
import online.prostobank.clients.domain.events.ClientNeedRegisterInKeycloakFofDbo;
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
@RequiredArgsConstructor
@Component
public class AfterReserveAction implements Action<ClientStates, ClientEvents> {
    private final ApplicationEventPublisher bus;

    @Override
    public void execute(StateContext<ClientStates, ClientEvents> context) {
        AccountApplication application = Optional.ofNullable(context)
                .map(StateContext::getExtendedState)
                .map(ExtendedState::getVariables)
                .map(m -> m.get(MarkEnum.ACCOUNT_APPLICATION_ENTITY))
                .map(client -> (AccountApplication) client).get();

        log.info("Send ApplicationIsNewEvent event");
        bus.publishEvent(new ApplicationIsNewEvent(application));

        ClientValue client = application.getClient();
        bus.publishEvent(new ClientNeedRegisterInKeycloakFofDbo(
                application.getId(),
                client.getEmail(),
                client.getPhone(),
                client.getFirstName(),
                client.getSurname(),
                client.getSecondName(),
                client.getInn()));
    }
}
