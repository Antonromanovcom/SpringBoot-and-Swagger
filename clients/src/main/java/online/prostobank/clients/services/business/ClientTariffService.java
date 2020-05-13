package online.prostobank.clients.services.business;

import online.prostobank.clients.domain.business_service.ClientBusinessTariffEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface ClientTariffService {

    /**
     * Получение информации о тарифах клиента
     * @param clientId - id клиента
     * @return список тарифов клиента
     */
    Optional<List<ClientBusinessTariffEntity>> getInfoClientTariff(@NotNull Long clientId);

    /**
     * Получение списка тарифов клиента по сервису
     * @param serviceId - id сервиса
     * @param clientId - id клиента
     * @return список тарифов клиента
     */
    Optional<List<ClientBusinessTariffEntity>> getInfoClientServiceTariff(@NotNull Long serviceId, @NotNull Long clientId);

    /**
     * Установка тарифа в демо
     * @param clientId - id клиента
     * @param tariffId - id тарифа
     * @return демо тариф
     */
    Optional<ClientBusinessTariffEntity> setDemo(@NotNull Long clientId, @NotNull Long tariffId);

    /**
     * Оплата тарифа
     * @param clientId - id клиента
     * @param tariffId - id тарифа
     * @return оплаченный тариф
     */
    Optional<ClientBusinessTariffEntity> setPay(@NotNull Long clientId, @NotNull Long tariffId);

    /**
     * Установка даты первого захода
     * @param clientId - id клиента
     * @param tariffId - id тарифа
     * @return - тариф, по которому произведен заход
     */
    Optional<ClientBusinessTariffEntity> setFirstEntrance(@NotNull Long clientId, @NotNull Long tariffId, @NotNull Long firstEntranceDate);

    /**
     * Установка даты активации для клиента
     *
     * @param clientKeyCloakId - id клиента в Кейклоке
     * @param tariffId - id тарифа
     * @param activationDate - дата активации
     * @return - тариф, по которому произведен заход
     */
    Optional<ClientBusinessTariffEntity> setActivationDate(@NotNull String clientKeyCloakId, @NotNull Long tariffId, @NotNull Long activationDate);

}
