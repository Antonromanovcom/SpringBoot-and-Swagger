package online.prostobank.clients.connectors.api;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Определение города по параметру
 */
public interface CityByIpDetector {

    /**
     * Определение города по ip
     * @param ip - ip адвес
     * @return - город
     */
    Optional<String> getCityByIpOrDefault(@NotNull(message = "Парамерт не задан") String ip);
}
