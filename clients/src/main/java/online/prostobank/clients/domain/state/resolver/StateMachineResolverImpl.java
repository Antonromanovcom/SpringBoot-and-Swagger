package online.prostobank.clients.domain.state.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.state.NextStatusInfoDTO;
import online.prostobank.clients.domain.state.persist.custom.interfaces.CustomMachinePersister;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateContext;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;


/**
 * Сервис для взаимодействя со стейт-машиной
 *
 * @param <StatesT> состояния стейт-машины
 * @param <EventsT> события стейт-машины
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StateMachineResolverImpl<StatesT, EventsT> implements StateMachineResolver<StatesT, EventsT> {

    private final StateMachineFactory<StatesT, EventsT> stateMachineFactory;
    private final CustomMachinePersister<StatesT, EventsT, String> stateCustomMachinePersister;


    @Override
    public @NotNull List<NextStatusInfoDTO<StatesT, EventsT>> getAvailableTransition(StateMachine<StatesT, EventsT> stateMachine) {

        return stateMachine.getTransitions()
                .stream()
                .filter(t -> isTransitionSourceFromCurrentState(t, stateMachine))
                .map(getStateInfo(stateMachine))
                .collect(toList());
    }

    @Override
    public @NotNull StateMachine<StatesT, EventsT> getStateMachine(@NotNull String idStateMachine,
                                                          @NotNull StatesT statesT,
                                                          @NotNull Map<Object, Object> context) {
        StateMachine<StatesT, EventsT> stateMachine = stateMachineFactory.getStateMachine(idStateMachine);
        try {
            stateCustomMachinePersister.restore(stateMachine, idStateMachine);
        } catch (Exception e) {
            log.info("Невозможно вернуть стейт-машину из кэша... {}", e.getMessage());
        }

        if(null == stateMachine.getId()) {
            stateMachine = stateMachineFactory.getStateMachine(idStateMachine);
        }

            stateMachine.stop();
            stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(access -> access.resetStateMachine( new DefaultStateMachineContext<>(
                            statesT, null, null, new DefaultExtendedState(context), null, idStateMachine)));
            stateMachine.start();
           saveStateMachine(stateMachine, idStateMachine);

        return stateMachine;
    }

    @NotNull
    private Function<Transition<StatesT, EventsT>, NextStatusInfoDTO<StatesT, EventsT>> getStateInfo(StateMachine<StatesT, EventsT> stateMachine){
        return (t) -> {
            State<StatesT, EventsT> target = t.getTarget();
            Trigger<StatesT, EventsT> trigger = t.getTrigger();
            boolean guardCondition = evaluateGuardCondition(stateMachine, t);
            return new NextStatusInfoDTO<>(target.getId(), trigger.getEvent(), guardCondition, "");
        };
    }

    @Override
    public boolean saveStateMachine(StateMachine<StatesT, EventsT> stateMachine, Object contextObj) {
        try {
            stateCustomMachinePersister.persist(stateMachine, stateMachine.getId());
            return true;
        } catch (Exception e) {
            log.info("Невозможно сохранить стейт-машину в кэш... {}", e.getMessage());
            return false;
        }

    }


    @Scheduled(cron = "${statemachine.clear_context}")
    @Override
    public void cleanCashStateMachineFactory() {
        stateCustomMachinePersister.clearContext();
    }


    private boolean isTransitionSourceFromCurrentState(Transition<StatesT, EventsT> transition,
                                                       StateMachine<StatesT, EventsT> stateMachine) {

        return stateMachine.getState().getId() == transition.getSource().getId();
    }


    private boolean evaluateGuardCondition(StateMachine<StatesT, EventsT> stateMachine,
                                           Transition<StatesT, EventsT> transition) {

        if (transition.getGuard() == null) {
            return true;
        }

        StateContext<StatesT, EventsT> context = makeStateContext(stateMachine, transition);

        try {
            return transition.getGuard().evaluate(context);
        } catch (Exception e) {
            return false;
        }
    }


    @NotNull
    private DefaultStateContext<StatesT, EventsT> makeStateContext(StateMachine<StatesT, EventsT> stateMachine,
                                                                   Transition<StatesT, EventsT> transition) {

        return new DefaultStateContext<>(StateContext.Stage.TRANSITION,
                null,
                null,
                stateMachine.getExtendedState(),
                transition,
                stateMachine,
                stateMachine.getState(),
                transition.getTarget(),
                null);
    }
}
