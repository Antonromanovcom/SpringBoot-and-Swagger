package online.prostobank.clients.connectors.api;

import club.apibank.connectors.exceptions.EgrulServiceException;

import java.io.IOException;

/**
 * Получение данных из ЕГРИП
 */
public interface IEgripService {
    /**
     * Получение данных по инн
     * @param inn - ИНН
     * @throws EgrulServiceException исключение в работе сервиса ЕГРЮЛ
     */
    String getInfoRaw(String inn) throws InterruptedException, EgrulServiceException, IOException;

    KonturService.InfoResult getInfo(String inn) throws InterruptedException, EgrulServiceException, IOException;
}
