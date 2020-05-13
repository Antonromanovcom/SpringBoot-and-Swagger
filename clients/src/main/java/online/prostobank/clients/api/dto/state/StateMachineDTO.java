package online.prostobank.clients.api.dto.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import online.prostobank.clients.domain.state.state.ClientStates;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class StateMachineDTO {
    private long id;
    private String name;
    private ClientStates state;
    private String taxNumber;
    private HashMap<Object, Object> clientContext;
}
