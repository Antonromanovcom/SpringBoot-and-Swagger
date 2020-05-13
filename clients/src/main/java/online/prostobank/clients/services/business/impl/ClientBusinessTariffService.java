package online.prostobank.clients.services.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.business_service.BusinessServiceEntity;
import online.prostobank.clients.domain.business_service.BusinessTariffEntity;
import online.prostobank.clients.domain.business_service.ClientBusinessTariffEntity;
import online.prostobank.clients.domain.business_service.ClientServiceAvailableEntity;
import online.prostobank.clients.domain.client.ClientKeycloak;
import online.prostobank.clients.domain.client.ClientKeycloakRepository;
import online.prostobank.clients.domain.repository.business.BusinessTariffRepository;
import online.prostobank.clients.domain.repository.business.ClientBusinessServiceRepository;
import online.prostobank.clients.domain.repository.business.ClientBusinessTariffRepository;
import online.prostobank.clients.services.business.ClientTariffService;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientBusinessTariffService implements ClientTariffService {

    private final ClientBusinessTariffRepository clientBusinessTariffRepository;
    private final ClientBusinessServiceRepository clientBusinessServiceRepository;
    private final ClientKeycloakRepository clientKeycloakRepository;
    private final BusinessTariffRepository tariffRepository;


    @Override
    public Optional<List<ClientBusinessTariffEntity>> getInfoClientTariff(@NotNull Long clientId) {
        return clientBusinessTariffRepository.getClientListTariff(clientId);

    }

    @Override
    public Optional<List<ClientBusinessTariffEntity>> getInfoClientServiceTariff(@NotNull Long serviceId, @NotNull Long clientId) {
        return clientBusinessTariffRepository.getClientListServiceTariff(serviceId, clientId);
    }

    @Override
    public Optional<ClientBusinessTariffEntity> setDemo(@NotNull Long clientId, @NotNull Long tariffId) {
        Optional<BusinessTariffEntity> businessTariffEntity = tariffRepository.findById(tariffId);
        Optional<ClientServiceAvailableEntity> clientService = clientBusinessServiceRepository.getClientService(clientId, businessTariffEntity.map(BusinessTariffEntity::getBusinessService)
                .map(BusinessServiceEntity::getId)
                .orElse(-1L)
        );

        if (businessTariffEntity.isPresent() && clientService.isPresent() && clientService.map(ClientServiceAvailableEntity::isAvailable).get()) {
            BusinessTariffEntity tariffEntity = businessTariffEntity.get();
            Instant endDate = tariffEntity.getEndDate(Instant.now(), tariffEntity.getDurationDemo());
            Optional<List<ClientBusinessTariffEntity>> clientBusinessTariffEntityList = clientBusinessTariffRepository.getClientListServiceTariff(clientService.get().getBusinessService().getId(), clientId);
            if (clientBusinessTariffEntityList.isPresent()) {
                long countOfDemo = clientBusinessTariffEntityList.get()
                        .stream()
                        .filter(ClientBusinessTariffEntity::isDemo)
                        .count();
                long countOfActivePayed = clientBusinessTariffEntityList.get()
                        .stream()
                        .filter(ClientBusinessTariffEntity::isPayed)
                        .filter(ClientBusinessTariffEntity::isActive)
                        .count();

                if (countOfDemo == 0 || countOfActivePayed == 0) {
                    return Optional.of(clientBusinessTariffRepository.save(new ClientBusinessTariffEntity(clientService.get(),
                            tariffEntity,
                            true,
                            false,
                            Instant.now(),
                            endDate,
                            endDate)));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.of(clientBusinessTariffRepository.save(
                        new ClientBusinessTariffEntity(clientService.get(),
                                tariffEntity,
                                true,
                                false,
                                Instant.now(),
                                endDate,
                                endDate)));
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ClientBusinessTariffEntity> setPay(@NotNull Long clientId, @NotNull Long tariffId) {
        Optional<BusinessTariffEntity> businessTariffEntity = tariffRepository.findById(tariffId);
        Optional<ClientServiceAvailableEntity> clientService = clientBusinessServiceRepository.getClientService(clientId, businessTariffEntity.map(BusinessTariffEntity::getBusinessService)
                .map(BusinessServiceEntity::getId)
                .orElse(-1L)
        );

        if (businessTariffEntity.isPresent() && clientService.isPresent() && clientService.map(ClientServiceAvailableEntity::isAvailable).get()) {
            BusinessTariffEntity tariffEntity = businessTariffEntity.get();
            // Instant endDate = tariffEntity.getEndDate(Instant.now(), tariffEntity.getDurationPayed());
            Optional<List<ClientBusinessTariffEntity>> clientBusinessTariffEntityList = clientBusinessTariffRepository.getClientTariffById(clientId, tariffId);
            if (clientBusinessTariffEntityList.isPresent()) {

                clientBusinessTariffEntityList.get()
                        .stream()
                        .filter(ClientBusinessTariffEntity::isDemo)
                        .filter(ClientBusinessTariffEntity::isActive)
                        .map(entity -> entity.setDateClose(null))
                        .forEach(clientBusinessTariffRepository::save);

                long countOfActivePayed = clientBusinessTariffEntityList.get()
                        .stream()
                        .filter(ClientBusinessTariffEntity::isPayed)
                        .filter(ClientBusinessTariffEntity::isActive)
                        .count();
                /**
                 * 24.12.2019 a.romanov. Так как нам очень важно отделить три разных факта:
                 *  - факт оплаты платежкой
                 *  - факт того, что платежка прошла
                 *  - факт входа в МД (именно этот факт делает тариф активированным).
                 *
                 *  ...у нас с одной стороны возникает проблема отдельной передачи этой даты, с другой стороны возникают вопросы:
                 *  а только ли с МД мы будем работать? А будут ли другие сервисы? А так же у них будет определяться активация
                 *  или достаточно будет при переключении тарифа в isPaid сразу поставить сегодняшнюю дату?
                 *
                 */
                if (countOfActivePayed == 0) {
                    return Optional.of(clientBusinessTariffRepository.save(new ClientBusinessTariffEntity(clientService.get(),
                            tariffEntity,
                            false,
                            true,
                            null,
                            null,
                            null)));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.of(clientBusinessTariffRepository.save(
                        new ClientBusinessTariffEntity(clientService.get(),
                                tariffEntity,
                                false,
                                true,
                                null,
                                null,
                                null)));
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ClientBusinessTariffEntity> setFirstEntrance(@NotNull Long clientId, @NotNull Long tariffId,
                                                                 @NotNull Long firstEntranceDate) {
        Optional<BusinessTariffEntity> businessTariffEntity = tariffRepository.findById(tariffId);
        Optional<ClientServiceAvailableEntity> clientService = clientBusinessServiceRepository.getClientService(clientId,
                businessTariffEntity.map(BusinessTariffEntity::getBusinessService)
                        .map(BusinessServiceEntity::getId)
                        .orElse(-1L)
        );

        if (businessTariffEntity.isPresent() && clientService.isPresent() && clientService
                .map(ClientServiceAvailableEntity::isAvailable)
                .get()) {

            // Если есть связка Клиент - Тариф - Услуга
            Optional<List<ClientBusinessTariffEntity>> clientBusinessTariffEntityList = clientBusinessTariffRepository.getClientTariffById(clientId, tariffId);
            if (clientBusinessTariffEntityList.isPresent()) {

                List<ClientBusinessTariffEntity> patchedWithFirstEntranceDateList = clientBusinessTariffEntityList.get()
                        .stream()
                        .filter(ClientBusinessTariffEntity::isPayed)
                        .filter(ClientBusinessTariffEntity::isActive)
                        .map(entity -> {
                            entity.setFirstEntrance(Instant.ofEpochMilli(firstEntranceDate));
                            return clientBusinessTariffRepository.saveAndFlush(entity);
                        }).collect(Collectors.toList());

                if (patchedWithFirstEntranceDateList.size() != 0) {
                    /**
                     * Данная фича скорее всего будет работать только для МД. А для МД вряд ли возможна ситуация
                     * когда в clientBusinessTariffRepository будет несколько записей для одного cleintId, tarifId и
                     * при этом active и isPayed.
                     */
                    return Optional.of(patchedWithFirstEntranceDateList.get(0));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ClientBusinessTariffEntity> setActivationDate(@NotNull String clientKeyCloakId, @NotNull Long tariffId, @NotNull Long activationDate) {
        // Отыщем клиента в Кейклоке по client_KeyCloak_Id
        UUID keycloakId = UUID.fromString(clientKeyCloakId);
        Optional<ClientKeycloak> keycloakClient = clientKeycloakRepository.findByKeycloakId(keycloakId);

        if (keycloakClient.isPresent()) { // если клиент нашелся

            Optional<BusinessTariffEntity> businessTariffEntity = tariffRepository.findById(tariffId); // достаем тариф по tariffId
            // Достаем сервис (по сути - партнера, например МД). Точнее говоря, достаем связку id-клиента <-> id-сервиса
            Optional<ClientServiceAvailableEntity> clientService = clientBusinessServiceRepository.getClientService(keycloakClient.get().getClientId(),
                    businessTariffEntity.map(BusinessTariffEntity::getBusinessService)
                            .map(BusinessServiceEntity::getId)
                            .orElse(-1L)
            );

            // Проверяем, что тариф такой нашелся, услуга (сервис) для клиента доступен, сервис такой вообще имеется
            if (businessTariffEntity.isPresent() && clientService.isPresent() && clientService.map(ClientServiceAvailableEntity::isAvailable).get()) {
                BusinessTariffEntity tariffEntity = businessTariffEntity.get(); // достаем тариф
                Instant endDate = tariffEntity.getEndDate(Instant.ofEpochMilli(activationDate), tariffEntity.getDurationPayed());

                // Вытаскиваем все свзяки "связка id-клиента <-> id-сервиса" <-> id-тарифа
                Optional<List<ClientBusinessTariffEntity>> clientBusinessTariffEntityList = clientBusinessTariffRepository
                        .getClientTariffById(keycloakClient.get().getClientId(), tariffId);

                // Если связки нашлись
                if (clientBusinessTariffEntityList.isPresent()) {

                    List<ClientBusinessTariffEntity> patchedWithActivationDateList = clientBusinessTariffEntityList.get()
                            .stream()
                            .filter(ClientBusinessTariffEntity::isPayed)
                            .filter(c -> c.getDateBegin() == null)
                            .map(entity -> {
                                entity.setDateBegin(Instant.ofEpochMilli(activationDate));
                                entity.setDateEnd(endDate);
                                entity.setDateClose(endDate);
                                return clientBusinessTariffRepository.saveAndFlush(entity);
                            }).collect(Collectors.toList());

                    if (patchedWithActivationDateList.size() != 0) {
                        return Optional.of(patchedWithActivationDateList.get(0));
                    } else {
                        log.error("Не найдено ни одного тарифа для изменения!");
                        return Optional.empty();
                    }


                } else {
                    log.error("Не найдено ни одной связки [связка id-клиента <-> id-сервиса] <-> id-тарифа");
                    return Optional.empty();
                }
            } else {
                log.error("Не найден тариф, сервис или связка сервис-клиент");
                return Optional.empty();
            }

            } else { // если клиент не нашелся
                log.error("Клиент с KeyCloak Id: {} не найден!", clientKeyCloakId);
                return Optional.empty();
            }
    }
}
