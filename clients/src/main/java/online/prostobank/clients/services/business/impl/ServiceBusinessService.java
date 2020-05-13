package online.prostobank.clients.services.business.impl;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.business.BusinessServiceDTO;
import online.prostobank.clients.domain.business_service.BusinessServiceEntity;
import online.prostobank.clients.domain.business_service.ClientServiceAvailableEntity;
import online.prostobank.clients.domain.repository.business.BusinessServiceRepository;
import online.prostobank.clients.domain.repository.business.ClientBusinessServiceRepository;
import online.prostobank.clients.services.business.ServiceBusiness;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceBusinessService implements ServiceBusiness {

    private final BusinessServiceRepository businessServiceRepository;
    private final ClientBusinessServiceRepository clientBusinessServiceRepository;


    @Override
    public Optional<List<ClientServiceAvailableEntity>> userServices(@NotNull Long clientId) {
        return clientBusinessServiceRepository.clientListServices(clientId);
    }

    @Override
    public Optional<BusinessServiceDTO> getService(@NotNull Long serviceId) {
        return businessServiceRepository
                .findById(serviceId)
                .map(BusinessServiceDTO::getDto);
    }

    @Override
    public Optional<List<BusinessServiceEntity>> getAllServices() {
        return Optional.of(businessServiceRepository.findAll());
    }

    @Override
    public Optional<BusinessServiceDTO> saveService(@NotNull BusinessServiceDTO dto) {
        return Optional.of(dto)
                .map(BusinessServiceDTO::getId)
                .map(id -> Optional.of(id).orElse(-1L))
                .map(id -> businessServiceRepository
                        .findById(id)
                        .orElseGet(BusinessServiceEntity::new))
                .map(businessServiceEntity -> businessServiceEntity.setName(dto.getName()))
                .map(businessServiceRepository::save)
                .map(BusinessServiceDTO::getDto);
    }

    @Override
    @Transactional
    public Optional<List<ClientServiceAvailableEntity>> turnOn(@NotNull Long clientId, @NotNull Long serviceId) {
        return switchAvailableService(clientId, serviceId, true);
    }


    @Override
    public Optional<List<ClientServiceAvailableEntity>> turnOff(@NotNull Long clientId, @NotNull Long serviceId) {
        return switchAvailableService(clientId, serviceId, false);
    }

    private Optional<List<ClientServiceAvailableEntity>> switchAvailableService(@NotNull Long clientId, @NotNull Long serviceId, boolean b) {
        Optional<ClientServiceAvailableEntity> clientService = clientBusinessServiceRepository.getClientService(clientId, serviceId);
        if (clientService.isPresent()) {
            ClientServiceAvailableEntity clientServiceAvailableEntity = clientService.get();
            clientServiceAvailableEntity.switchOn(b);
            clientBusinessServiceRepository.save(clientServiceAvailableEntity);
            return clientBusinessServiceRepository.clientListServices(clientId);
        } else {
            Optional<BusinessServiceEntity> businessEntity = businessServiceRepository
                    .findById(serviceId);
            if (businessEntity.isPresent()) {
                BusinessServiceEntity businessServiceEntity = businessEntity.get();
                ClientServiceAvailableEntity entity = clientBusinessServiceRepository.save(new ClientServiceAvailableEntity(businessServiceEntity, clientId, b));
                return Optional.of(Collections.singletonList(entity));
            } else {
                return Optional.empty();
            }
        }
    }
}
