package online.prostobank.clients.services.state;

import online.prostobank.clients.api.dto.state.NextStatusInfoDTO;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface StateMachineServiceI {
	/**
	 * Возможные переходы статусов
	 *
	 * @param clientId
	 * @return
	 */
	Optional<List<NextStatusInfoDTO<ClientStates, ClientEvents>>> getInfoNextStatus(@NotNull Long clientId);

	/**
	 * Установить статус
	 *
	 * @param dto
	 * @return
	 */
	@Transactional
	Optional<StatusInfoDTO> setState(StateSetterDTO dto);
}
