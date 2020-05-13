package online.prostobank.clients.domain.state.state;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Статусы клиента
 */
@AllArgsConstructor
@Getter
public enum ClientStates {

	NEW_CLIENT("Создан",                                1, new int[]{2, 3, 5}),
	CONTACT_INFO_CONFIRMED("Информация подтверждена",   2, new int[]{3, 4, 5}),
	NO_ANSWER("Недозвон",                               3, new int[]{1, 2, 6, 7, 12}),
	CHECK_LEAD("Проверка лида",                         4, new int[]{5, 6, 12}),
	CLIENT_DECLINED("Отказ клиента",                    5, new int[]{1, 2, 6, 7}),
	WAIT_FOR_DOCS("Ожидание документов",                6, new int[]{3, 5, 7, 12}),
	DOCUMENTS_EXISTS("Документы вложены",               7, new int[]{3, 5, 9, 12}),
	REQUIRED_DOCS("Дозапрос документов",                8, new int[]{3, 5, 9, 12}),
	MANAGER_PROCESSING("В процессе открытия счёта",     9, new int[]{5, 8, 10, 11, 12}),
	ACTIVE_CLIENT("Счет открыт",                       10, new int[]{11}),
	INACTIVE_CLIENT("Счет закрыт",                     11, new int[]{}),
	AUTO_DECLINED("Автоматический отказ",              12, new int[]{}),
	;

	private final String ruName;
	// APIKUB-2005, согласно графу статусов
	private final int index;
	private final int[] next;
}
