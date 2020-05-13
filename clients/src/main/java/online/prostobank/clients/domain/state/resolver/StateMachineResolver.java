package online.prostobank.clients.domain.state.resolver;

import online.prostobank.clients.api.dto.state.NextStatusInfoDTO;
import org.springframework.statemachine.StateMachine;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface StateMachineResolver<S, E> {

    /**
     * Возвращает следующие возможные статусы из стейт-машины
     *
     * @param stateMachine стейт-машина
     *
     * @return Коллекция событий
     */
    @NotNull
    List<NextStatusInfoDTO<S, E>> getAvailableTransition(StateMachine<S, E> stateMachine);

    /**
     * Возвращает стейт-машину из кэша или создает
     * стейт-машину, переводит ее в нужный статус и кэширует
     * @param idStateMachine id стейт-машины
     * @param state Актуальное состояние
     * @param context контекст клиента
     * @return стейт-машину
     */
    @NotNull
    StateMachine<S,E> getStateMachine(@NotNull String idStateMachine, @NotNull S state, @NotNull Map<Object, Object> context);

    /**
     * Сохранение стейт-машины в кэш приложения
     *
     * @param stateMachine
     * @param contextObj
     * @return
     */
    boolean saveStateMachine(StateMachine<S, E> stateMachine, Object contextObj);

    /**
     * Очистка кэша стейт-машины
     */
    void cleanCashStateMachineFactory();
}
