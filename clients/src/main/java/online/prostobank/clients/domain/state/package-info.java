/**
 * Конфигурация {@link org.springframework.statemachine.config.StateMachineConfigurerAdapter},
 * доменная логика описания обработки событий происходящих с заявками {@link online.prostobank.clients.domain.state.action}
 * статусы клиента {@link online.prostobank.clients.domain.state.state},
 * события заявок {@link online.prostobank.clients.domain.state.event},
 * предварительная валидация данных перед обработкой {@link online.prostobank.clients.domain.state.guard}
 * сервис для взаимодействия со стейт-машиной {@link online.prostobank.clients.domain.state.resolver}
 * кастомный кэш-персистенс сервис {@link online.prostobank.clients.domain.state.persist}
 * интерсептор для логирования перехода состояний {@link online.prostobank.clients.domain.state.config}
 */
package online.prostobank.clients.domain.state;