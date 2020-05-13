package online.prostobank.clients.domain.state.persist.custom.interfaces;

import org.springframework.statemachine.StateMachinePersist;

public interface CustomStateMachinePersist<S, E, T> extends StateMachinePersist<S, E, T> {

    /**
     * Очистка кэша стейт-машины
     */
    void cleanContext();
}
