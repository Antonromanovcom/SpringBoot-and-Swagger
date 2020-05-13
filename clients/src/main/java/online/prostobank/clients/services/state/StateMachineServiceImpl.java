package online.prostobank.clients.services.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.state.NextStatusInfoDTO;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.status_log.StatusHistoryRepository;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.resolver.StateMachineResolver;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineServiceImpl implements StateMachineServiceI {

	private final StateMachineResolver<ClientStates, ClientEvents> stateMachineResolver;
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final StatusHistoryRepository statusHistoryRepository;

	@Override
	public Optional<List<NextStatusInfoDTO<ClientStates, ClientEvents>>> getInfoNextStatus(@NotNull Long clientId) {
		return accountApplicationRepository.findById(clientId)
				.map(this::getStateMachine)
				.map(stateMachine -> {
							ClientStates currentState = stateMachine.getState().getId();
							return stateMachineResolver.getAvailableTransition(stateMachine)
									.stream()
									.filter(it -> isNextStateVisible(currentState, it.getNextState()))
									.peek(next ->
											next.setName(
													next.getNextState().getRuName()
											)
									)
									.collect(Collectors.toList());
						}
				);
	}

	//не показываем в качестве следующего "автоотказ"
	//в состоянии "контакт подтвержден" не показываем в качестве следующего "ожидание документов"
	//в состоянии "в процессе открытия счета" не показываем "счет закрыт" и "счет открыт"
	private boolean isNextStateVisible(ClientStates currentState, ClientStates nextState) {
		return !nextState.equals(ClientStates.AUTO_DECLINED)
				&&
				!(currentState.equals(ClientStates.CONTACT_INFO_CONFIRMED) && nextState.equals(ClientStates.WAIT_FOR_DOCS))
				&&
				!(currentState.equals(ClientStates.MANAGER_PROCESSING) && (nextState.equals(ClientStates.ACTIVE_CLIENT) || nextState.equals(ClientStates.INACTIVE_CLIENT)));
	}

	@Transactional
	@Override
	public Optional<StatusInfoDTO> setState(StateSetterDTO dto) {
		return accountApplicationRepository.findById(dto.getClientId())
				.map(client -> {
					StateMachine<ClientStates, ClientEvents> stateMachine = getStateMachine(client);
					ClientStates previousState = stateMachine.getState().getId();
					stateMachine.sendEvent(dto.getEvent());

					ClientStates newState = stateMachine.getState().getId();

					statusHistoryRepository.insertStatusHistory(client.getId(), previousState, dto.getEvent(),
							newState, SecurityContextHelper.getCurrentUsername(), dto.getCauseMessage());

					Status newLegacyState = client.getStatus().getLegacyState(newState);
					log.info("newState: {}, newLegacyState: {}", newState, newLegacyState);
					//костыль для цепочки проверок, иначе портится статус ERR_AUTO_DECLINE при подгрузке документов, например
					if (!previousState.equals(newState)) {
						client.setStatus(new StatusValue(newLegacyState));
						client.setClientState(newState);
					}
					repositoryWrapper.saveAccountApplication(client);
					return new StatusInfoDTO(newState, dto.getClientId());
				});
	}

	@NotNull
	private StateMachine<ClientStates, ClientEvents> getStateMachine(AccountApplication client) {
		return stateMachineResolver.getStateMachine(
				String.valueOf(client.getId()),
				client.getClientState(),
				client.getContext()
		);
	}
}
