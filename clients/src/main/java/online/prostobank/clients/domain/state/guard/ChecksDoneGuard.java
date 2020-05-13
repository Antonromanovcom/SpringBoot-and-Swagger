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
public class ChecksDoneGuard implements Guard<ClientStates, ClientEvents> {
    @Override
    public boolean evaluate(StateContext<ClientStates, ClientEvents> context) {
        return Optional.ofNullable(context)
                .map(StateContext::getExtendedState)
                .map(ExtendedState::getVariables)
                .map(var -> Optional.ofNullable(var.get(MarkEnum.KYC_MARK))
                        .map(Object::toString)
                        .map(Boolean::valueOf)
                        .orElse(false)

                        && Optional.ofNullable(var.get(MarkEnum.P550_MARK))
                        .map(Object::toString)
                        .map(Boolean::valueOf)
                        .orElse(false)

// отключена проверка арестов, как имеющая высокую вероятность аварии, что препятствует анкете, а также попаданию клиента
// в статус "ожидание документов"
//                        && Optional.ofNullable(var.get(MarkEnum.ARRESTS_MARK))
//                        .map(Object::toString)
//                        .map(Boolean::valueOf)
//                        .orElse(false)

//отключена проверка результата проверки паспорта т.к. до вложения документов сведения о паспорте может не быть
//                        && Optional.ofNullable(var.get(MarkEnum.PASSPORT_MARK))
//                        .map(Object::toString)
//                        .map(Boolean::valueOf)
//                        .orElse(false)
                )
                .orElse(false);
    }
}
