package online.prostobank.clients.services.business;

import online.prostobank.clients.api.business.BusinessServiceDTO;
import online.prostobank.clients.domain.business_service.BusinessServiceEntity;
import online.prostobank.clients.domain.business_service.ClientServiceAvailableEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface ServiceBusiness {

    /**
     * Список сервисов у пользователя
     * @param clientId - id клиента
     * @return лист действующих/не действующих сервисов клиента
     */
    Optional<List<ClientServiceAvailableEntity>> userServices(@NotNull  Long clientId);

    /**
     * Информация по сервису
     * @param serviceId - id бизнес-сервиса
     * @return информация о сервисе по id
     */
    Optional<BusinessServiceDTO> getService(@NotNull Long serviceId);

    /**
     * Возвращение списка всех сервисов
     * @return список всех сервисов
     */
    Optional<List<BusinessServiceEntity>> getAllServices();

    /**
     * Изменение/сохранение информации о сервисе
     * @param dto - новая информация/ отредактируемая информация
     * @return сохранение/изменение сервиса
     */
    Optional<BusinessServiceDTO> saveService(@NotNull BusinessServiceDTO dto);

    /**
     * Включение сервиса у клиента
     * @param clientId - id клиента
     * @param serviceId - id бизнес-сервиса
     * @return лист действующих/не действующих сервисов клиента
     */
    Optional<List<ClientServiceAvailableEntity>> turnOn(@NotNull Long clientId, @NotNull Long serviceId);

    /**
     * Выключение сервиса у клиента
     * @param clientId - id клиента
     * @param serviceId - id бизнес-сервиса
     * @return лист действующих/не действующих сервисов клиента
     */
    Optional<List<ClientServiceAvailableEntity>> turnOff(@NotNull Long clientId, @NotNull Long serviceId);
}
