package online.prostobank.clients.domain.state.persist.custom.interfaces;

import org.springframework.statemachine.persist.StateMachinePersister;

public interface CustomMachinePersister<S, E, T> extends StateMachinePersister<S, E, T> {
    /**
     * Очистка кэша стейт-машины
     */
    void clearContext();
}
