package online.prostobank.clients.connectors.api;

import javax.validation.constraints.NotEmpty;

/**
 * Проверки "РосCтат"
 */
public interface RosstatService {
    /**
     * Получение данных по инн
     * @param inn - ИНН
     * @throws Exception все исключения
     */
    @NotEmpty(message = "Парамерт не задан") String getData(@NotEmpty(message = "Парамерт не задан") String inn)
            throws Exception;
}
