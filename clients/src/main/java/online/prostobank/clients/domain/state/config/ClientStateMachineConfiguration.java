package online.prostobank.clients.domain.state.config;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.domain.state.action.*;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.guard.ChecksDoneGuard;
import online.prostobank.clients.domain.state.guard.ConfirmedGuard;
import online.prostobank.clients.domain.state.guard.SmsGuard;
import online.prostobank.clients.domain.state.listener.ClientStateMachineListener;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;

import java.util.EnumSet;

import static online.prostobank.clients.domain.state.event.ClientEvents.*;
import static online.prostobank.clients.domain.state.state.ClientStates.*;

/**
 * Конфигурация {@link StateMachineConfigurerAdapter},
 * описание транзитивности статусов
 */
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class ClientStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<ClientStates, ClientEvents> {

	private final SmsAction smsAction;
	private final NotCallingAction notCallingAction;
	private final DeclineAction declineAction;
	private final UnconfirmedAction unconfirmedAction;
	private final ConfirmedAction confirmedAction;
	private final CreateApplicationAction createApplicationAction;
	private final ChecksAction checksAction;
	private final Checks550Action checks550Action;
	private final AfterReserveAction afterReserveAction;
	private final NeedDocsAction needDocsAction;
	private final DocsAddedAction docsAddedAction;
	private final AccountOpenAction accountOpenAction;
	private final AccountCloseAction accountCloseAction;
	private final TryReserveAction tryReserveAction;
	private final MakeColdAction makeColdAction;
	private final AutoDeclineAction autoDeclineAction;
	private final SmsGuard smsGuard;
	private final ChecksDoneGuard checksDoneGuard;
	private final ConfirmedGuard confirmedGuard;
	private final DocumentsLoadedAction documentsLoadedAction;
	private final DboAccountChangeStatusAction accountChangeStatusAction;

	/**
	 * Конфигурация адаптера конечного автомата
	 *
	 * @param config конфигурация
	 * @throws Exception исключения
	 */
	@Override
	public void configure(StateMachineConfigurationConfigurer<ClientStates, ClientEvents> config)
			throws Exception {
		config.withConfiguration()
				.autoStartup(true)
				.listener(new ClientStateMachineListener());
	}

	/**
	 * Конфигурация обрабатываемых статусов конечным автоматом
	 *
	 * @param states обрабатываемые статусы
	 * @throws Exception исключение
	 */
	@Override
	public void configure(StateMachineStateConfigurer<ClientStates, ClientEvents> states) throws Exception {
		states
				.withStates()
				.initial(NEW_CLIENT)
				.states(EnumSet.allOf(ClientStates.class));
	}

	/**
	 * Конфигурация транзитивности статусов
	 *
	 * @param transitions переходы
	 * @throws Exception исключения
	 */
	@Override
	public void configure(StateMachineTransitionConfigurer<ClientStates, ClientEvents> transitions) throws Exception {
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from0 = transitions.withExternal();
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from1 = from1(from0);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from2 = from2(from1);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from3 = from3(from2);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from4 = from4(from3);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from5 = from5(from4);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from6 = from6(from5);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from7 = from7(from6);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from8 = from8(from7);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from9 = from9(from8);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from10 = from10(from9);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from11 = from11(from10);
		ExternalTransitionConfigurer<ClientStates, ClientEvents> from12 = from12(from11);
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from1(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from NEW_CLIENT
		return previous
				.source(NEW_CLIENT)
				.target(CONTACT_INFO_CONFIRMED)
				.event(CE_SMS)
				.action(smsAction)
//				.guard(smsGuard)

				.and()
				.withExternal()
				.source(NEW_CLIENT)
				.target(NO_ANSWER)
				.event(CLIENT_NOT_RESPONDING)
				.action(notCallingAction)

				.and()
				.withExternal()
				.source(NEW_CLIENT)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(NEW_CLIENT)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from2(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from CONTACT_INFO_CONFIRMED
		return previous
				.and()
				.withExternal()
				.source(CONTACT_INFO_CONFIRMED)
				.target(NO_ANSWER)
				.event(CLIENT_NOT_RESPONDING)
				.action(unconfirmedAction)

				.and()
				.withExternal()
				.source(CONTACT_INFO_CONFIRMED)
				.target(CHECK_LEAD)
				.event(CHECKS)
				.action(checksAction)
				.guard(confirmedGuard)

				.and()
				.withExternal()
				.source(CONTACT_INFO_CONFIRMED)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(CONTACT_INFO_CONFIRMED)
				.target(WAIT_FOR_DOCS)
				.event(CHECKS_DONE)
				.action(afterReserveAction)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(CONTACT_INFO_CONFIRMED)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from3(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from NO_ANSWER
		return previous
				.and()
				.withExternal()
				.source(NO_ANSWER)
				.target(NEW_CLIENT)
				.event(TO_NEW_CLIENT)

				.and()
				.withExternal()
				.source(NO_ANSWER)
				.target(CONTACT_INFO_CONFIRMED)
				.event(CONFIRMED)
				.action(confirmedAction)

				.and()
				.withExternal()
				.source(NO_ANSWER)
				.target(WAIT_FOR_DOCS)
				.event(TO_WAIT_FOR_DOCS)
				.action(checks550Action)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(NO_ANSWER)
				.target(DOCUMENTS_EXISTS)
				.event(AT_LEAST_ONE_DOCUMENTS_LOADED)

				.and()
				.withExternal()
				.source(NO_ANSWER)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from4(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from CHECK_LEAD
		return previous
				.and()
				.withExternal()
				.source(CHECK_LEAD)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)

				.and()
				.withExternal()
				.source(CHECK_LEAD)
				.target(WAIT_FOR_DOCS)
				.event(CHECKS_DONE)
				.action(afterReserveAction)
				.guard(checksDoneGuard)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(CHECK_LEAD)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(CHECK_LEAD)
				.target(CHECK_LEAD)
				.event(CHECKS)
				.action(checksAction)
				.guard(confirmedGuard)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from5(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from CLIENT_DECLINED
		return previous
				.and()
				.withExternal()
				.source(CLIENT_DECLINED)
				.target(NEW_CLIENT)
				.event(TO_NEW_CLIENT)

				.and()
				.withExternal()
				.source(CLIENT_DECLINED)
				.target(CONTACT_INFO_CONFIRMED)
				.event(CONFIRMED)

				.and()
				.withExternal()
				.source(CLIENT_DECLINED)
				.target(WAIT_FOR_DOCS)
				.event(TO_WAIT_FOR_DOCS)
				.action(checks550Action)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(CLIENT_DECLINED)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(CLIENT_DECLINED)
				.target(DOCUMENTS_EXISTS)
				.event(AT_LEAST_ONE_DOCUMENTS_LOADED)

//				.and()
//				.withExternal()
//				.source(CLIENT_DECLINED)
//				.target(INACTIVE_CLIENT)
//				.event(MAKE_COLD)
//				.action(makeColdAction)
//				.action(accountChangeStatusAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from6(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from WAIT_FOR_DOCS
		return previous
				.and()
				.withExternal()
				.source(WAIT_FOR_DOCS)
				.target(NO_ANSWER)
				.event(CLIENT_NOT_RESPONDING)
				.action(notCallingAction)

				.and()
				.withExternal()
				.source(WAIT_FOR_DOCS)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)

				.and()
				.withExternal()
				.source(WAIT_FOR_DOCS)
				.target(DOCUMENTS_EXISTS)
				.event(AT_LEAST_ONE_DOCUMENTS_LOADED)
				.action(documentsLoadedAction)

				.and()
				.withExternal()
				.source(WAIT_FOR_DOCS)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from7(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from DOCUMENTS_EXISTS
		return previous
				.and()
				.withExternal()
				.source(DOCUMENTS_EXISTS)
				.target(NO_ANSWER)
				.event(CLIENT_NOT_RESPONDING)
				.action(notCallingAction)

				.and()
				.withExternal()
				.source(DOCUMENTS_EXISTS)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)

				.and()
				.withExternal()
				.source(DOCUMENTS_EXISTS)
				.target(MANAGER_PROCESSING)
				.event(RESERVE)
				.action(checks550Action)
				.action(createApplicationAction)

				.and()
				.withExternal()
				.source(DOCUMENTS_EXISTS)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(WAIT_FOR_DOCS)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				//APIKUB-2186 возможность вручную вернуться из "вложен хотя бы один документ" в "ожидание документов"
				.and()
				.withExternal()
				.source(DOCUMENTS_EXISTS)
				.target(WAIT_FOR_DOCS)
				.event(NEED_DOCS)
				.action(accountChangeStatusAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from8(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from REQUIRED_DOCS
		return previous
				.and()
				.withExternal()
				.source(REQUIRED_DOCS)
				.target(NO_ANSWER)
				.event(CLIENT_NOT_RESPONDING)
				.action(notCallingAction)

				.and()
				.withExternal()
				.source(REQUIRED_DOCS)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)

				.and()
				.withExternal()
				.source(REQUIRED_DOCS)
				.target(MANAGER_PROCESSING)
				.event(DOCS_ADDED)
				.action(checks550Action)
				.action(docsAddedAction)

				.and()
				.withExternal()
				.source(REQUIRED_DOCS)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)

				.and()
				.withExternal()
				.source(REQUIRED_DOCS)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from9(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from MANAGER_PROCESSING
		return previous
				.and()
				.withExternal()
				.source(MANAGER_PROCESSING)
				.target(CLIENT_DECLINED)
				.event(CLIENT_DECLINE)
				.action(declineAction)

				.and()
				.withExternal()
				.source(MANAGER_PROCESSING)
				.target(REQUIRED_DOCS)
				.event(NEED_DOCS)
				.action(needDocsAction)

				.and()
				.withExternal()
				.source(MANAGER_PROCESSING)
				.target(ACTIVE_CLIENT)
				.event(ACCOUNT_OPEN)
				.action(accountOpenAction)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(MANAGER_PROCESSING)
				.target(INACTIVE_CLIENT)
				.event(ACCOUNT_CLOSE)
				.action(accountCloseAction)
				.action(accountChangeStatusAction)

				.and()
				.withExternal()
				.source(MANAGER_PROCESSING)
				.target(AUTO_DECLINED)
				.event(AUTO_DECLINE)
				.action(autoDeclineAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from10(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from ACTIVE_CLIENT
		return previous
				.and()
				.withExternal()
				.source(ACTIVE_CLIENT)
				.target(INACTIVE_CLIENT)
				.event(ACCOUNT_CLOSE)
				.action(accountCloseAction)
				.action(accountChangeStatusAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from11(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from INACTIVE_CLIENT
		return previous
//				.and()
//				.withExternal()
//				.source(INACTIVE_CLIENT)
//				.target(NEW_CLIENT)
//				.event(TRY_RESERVE)
//				.action(tryReserveAction)
				;
	}

	private ExternalTransitionConfigurer<ClientStates, ClientEvents> from12(ExternalTransitionConfigurer<ClientStates, ClientEvents> previous) throws Exception {
		// from AUTO_DECLINED
		return previous
//				.and()
//				.withExternal()
//				.source(AUTO_DECLINED)
//				.target(INACTIVE_CLIENT)
//				.event(MAKE_COLD)
//				.action(makeColdAction)
//				.action(accountChangeStatusAction)
				;
	}
}
