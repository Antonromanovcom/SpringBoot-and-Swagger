package online.prostobank.clients.api.dto.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.state.event.ClientEvents;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateSetterDTO {

    private Long clientId;
    private ClientEvents event;
    private String causeMessage;

    public StateSetterDTO(Long clientId, ClientEvents event) {
        this.clientId = clientId;
        this.event = event;
    }
}

