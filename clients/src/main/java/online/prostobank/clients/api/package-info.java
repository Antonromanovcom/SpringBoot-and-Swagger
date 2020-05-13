/**
 * Интерфейсы для взаимодействия с внешними системами
 *
 * Описание:
 *  - {@link online.prostobank.clients.api.anketa}
 *      работа с заявками на открытия счета в телемаркете,
 *      анкетой, формой регистрации заявки
 *
 *  - {@link online.prostobank.clients.api.client}
 *      работа с клиентами, сохранение, опросника клиента,
 *      перевод клиента в новый статус, получение карточки клиента,
 *      создание карточки клиента, проверка клиента,
 *      отправка сообщений пользователю, экспорт в PDF,
 *      смс подтверждения
 *
 *  - {@link online.prostobank.clients.api.dictionary}
 *      получение служебных словарей. Статусов, городов.
 *
 *  - {@link online.prostobank.clients.api.dto}
 *      модели данных для работы с интерфейсом
 *      {@link online.prostobank.clients.api}
 *
 *  - {@link online.prostobank.clients.api.account}
 *      работа с заявками, получение, изменение,
 *      деактивация, создание
 *  - {@link online.prostobank.clients.api.state}
 *      работа со стейт-машиной, получение возможных состояний переходов,
 *      переход на новый статус
 *
 *  - {@link online.prostobank.clients.api.system}
 *      внутренние служебные, отладочный и конфигурационные
 *      интерфейсы
 *
 */
package online.prostobank.clients.api;
