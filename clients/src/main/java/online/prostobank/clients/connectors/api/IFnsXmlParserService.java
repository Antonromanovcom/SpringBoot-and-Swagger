package online.prostobank.clients.connectors.api;

import javax.validation.constraints.NotNull;

/**
 * Парсер xml данных из ФНС
 */
public interface IFnsXmlParserService {
    /**
     * Подсчет количества сотрудников компании
     * @param inn - ИНН
     * @return - количество сотрудников
     */
    int getInfo(@NotNull(message = "Парамерт не задан") String inn);
}
