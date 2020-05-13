package online.prostobank.clients.domain.state.persist;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.persist.custom.interfaces.CustomStateMachinePersist;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.statemachine.StateMachineContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Персистирование контекста состояния конечного автомата
 */
@Slf4j
public class ClientStateMachinePersistProcessor implements CustomStateMachinePersist<ClientStates, ClientEvents, String> {
    private static final Map<String, StateMachineContext<ClientStates, ClientEvents>> contexts = new ConcurrentHashMap<>(50);

    /**
     * Запись контекста состояния
     * @param context контекст события
     * @param contextObj тип контекстного объекта
     */
    @Override
    public void write(final StateMachineContext<ClientStates, ClientEvents> context, final String contextObj) {
        log.trace("Persising context");
        contexts.put(contextObj, context);
    }

    /**
     * Чтение контекста состояния
     * @param contextObj тип контекстного объекта
     * @return конечный автомат
     */
    @Override
    public StateMachineContext<ClientStates, ClientEvents> read(final String contextObj) {
        log.trace("Reading context");
        return contexts.get(contextObj);
    }

    @Override
    public void cleanContext() {
        contexts.clear();
    }
}
