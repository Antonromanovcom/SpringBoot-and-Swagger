package online.prostobank.clients.services.anketa;

import online.prostobank.clients.api.dto.anketa.AccountApplicationDTO;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.PersonValue;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

/**
 * todo: doc
 */
public interface IAnticorruption {
    /**
     * Получение объекта города по наименованию
     * @param city - наименование города
     * @return - город {@link City}
     */
    @Nonnull City obtainCity(String city);

    /**
     * Получение объекта {@link ClientValue} клиента
     * @param phone - номер телефона
     * @return - {@link ClientValue}
     */
    @Nonnull ClientValue obtainClient(@NotNull(message = "Парамерт не задан") String phone);

    /**
     * todo: doc
     * @param pv
     * @param dto
     */
    void fillPerson(
            @NotNull(message = "Парамерт не задан") PersonValue pv,
            @NotNull(message = "Парамерт не задан") AccountApplicationDTO dto
    );
}
