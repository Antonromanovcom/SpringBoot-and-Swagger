package online.prostobank.clients.domain.business_service;

/**
 * Методы сервиса-бухгалтерии
 *
 */
public interface BusinessService {

    /**
     * Включение сервиса - бухгалтерии у клиента
     * @param sw включить/выключить
     * @return включено/выключено
     */
    boolean switchOn(boolean sw);


}
