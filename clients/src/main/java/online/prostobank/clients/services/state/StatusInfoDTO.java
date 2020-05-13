package online.prostobank.clients.services.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.state.state.ClientStates;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusInfoDTO {
    private ClientStates state;
    private Long clientId;
}
