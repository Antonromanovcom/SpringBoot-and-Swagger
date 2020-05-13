package online.prostobank.clients.connectors.api;

import online.prostobank.clients.domain.AccountApplication;

import javax.validation.constraints.NotNull;

/**
 * Проверка на аресты
 */
public interface IArrestsCheckService {
    /**
     * Проверка на аресты
     * @param accountApplication - заявка
     */
    void checkArrests(@NotNull(message = "Парамерт не задан") AccountApplication accountApplication);
}
