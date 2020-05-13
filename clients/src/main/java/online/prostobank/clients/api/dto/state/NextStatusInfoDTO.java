package online.prostobank.clients.api.dto.state;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NextStatusInfoDTO<S, T> {
    S nextState;
    T eventToMove;
    boolean canMove;
    String name;
}
