package online.prostobank.clients.domain.state.persist.custom;

import lombok.Getter;
import online.prostobank.clients.domain.state.persist.custom.interfaces.CustomMachinePersister;
import online.prostobank.clients.domain.state.persist.custom.interfaces.CustomStateMachinePersist;
import org.springframework.statemachine.persist.AbstractStateMachinePersister;

@Getter
public class CustomStateCustomMachinePersister<S, E, T> extends AbstractStateMachinePersister<S, E, T> implements CustomMachinePersister<S, E, T> {

    private CustomStateMachinePersist<S, E, T> stateMachinePersist;


    /**
     * Instantiates a new abstract state machine persister.
     *
     * @param stateMachinePersist the state machine persist
     */
    public CustomStateCustomMachinePersister(CustomStateMachinePersist<S, E, T> stateMachinePersist) {
        super(stateMachinePersist);
        this.stateMachinePersist = stateMachinePersist;
    }

    @Override
    public void clearContext() {
        stateMachinePersist.cleanContext();
    }
}
