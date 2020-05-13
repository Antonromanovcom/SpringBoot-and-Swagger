/**
 * Интерфейсы для внутреннего взаимодействия со сторонними
 * системами
 * - API всех коннекторов
 *      {@link online.prostobank.clients.connectors.api}
 *
 * - единый интерфейс работы с коннекторами
 *      {@link online.prostobank.clients.connectors.ExternalConnectors}
 *
 * - обновление базы просроченных паспортов по расписанию
 *      {@link online.prostobank.clients.connectors.expired_passports.ExpiredPassportsImporter}
 */
package online.prostobank.clients.connectors;