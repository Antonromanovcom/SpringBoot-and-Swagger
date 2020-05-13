package online.prostobank.clients.domain.business_service;

/**
 * Методы тарифа
 */
public interface BusinessTariff {

    /**
     * Метод для просмотра описания тарифа
     * @return описание тарифа
     */
    String description();

    /**
     * Метод для установки описания к тарифу
     * @return описание тарифа
     */
    String setDescription(String description);

    /**
     * Установка Демо для тарифа
     * @param demo подключение демо
     * @return установлено ли демо для тарифа
     */
    boolean demo(boolean demo);

    /**
     * Является ли подключение тарифа - демо
     * @return установлено ли демо для тарифа
     */
    boolean isDemo();

    /**
     * Оплачен тариф или нет
     * @return является ли тариф оплаченым
     */
    boolean isPayed();

    /**
     * Метод для оплаты тарифа
     * @param pay оплата тарифа
     * @return является ли тариф оплаченым
     */
    boolean pay(boolean pay);

    /**
     * Метод для просмотра оставшихся дней активного тарифа
     * @return Количество часов тарифа
     */
    Long active();

    /**
     * Метод, показывающий активен ли тариф у клиента
     * @return является ли данный тариф активным
     */
    boolean isActive();


}
