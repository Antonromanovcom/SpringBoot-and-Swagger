package online.prostobank.clients.domain.statuses;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;

import static online.prostobank.clients.domain.statuses.StatusInfo.*;

/**
 * @author yurij
 */
@Slf4j
public enum Status {

	/**
	 * Создана
	 */
	@StatusInfo(
			value = 1,
			name = "Создана",
			next = {2, 5, 16, 26},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	CONTACT_INFO_UNCONFIRMED,

	/**
	 * Подтвердили телефон
	 */
	@StatusInfo(
			value = 2,
			name = "Телефон подтверждён",
			next = {3, 16, 17, 26},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	CONTACT_INFO_CONFIRMED,

	/**
	 * Прошли и зарезервировали счет
	 */
	@StatusInfo(
			value = 3,
			name = "Резервирование счета",
			next = {4, 5, 16},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	RESERVING,

	@StatusInfo(
			value = 4,
			name = "Новая",
			next = {5, 7, 16},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	NEW,

	@StatusInfo(
			value = 5,
			name = "Недозвон",
			next = {4, 7, 16},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	NO_ANSWER,

	@StatusInfo(
			value = 7,
			name = "Ожидание документов",
			next = {5, 11, 16},
			allowedTo = {POS_ADMIN, POS_FRONT, POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_CONSULTANT})
	WAIT_FOR_DOCS,

	@StatusInfo(
			value = 8,
			name = "Отказ бэк-менеджера",
			next = {11},
			decline = true,
			allowedTo = {POS_ADMIN,
//					POS_BACK,
					POS_CONSULTANT},
			editable = {
//            		POS_BACK,
					POS_ADMIN})
	POS_BACK_REFUSED,

	@StatusInfo(
			value = 9,
			name = "На подписании",
			next = {7, 16, 24, 25},
			allowedTo = {POS_FRONT, POS_ADMIN,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_ADMIN, POS_CONSULTANT},
			editable = {POS_FRONT, POS_ADMIN,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_OUTER_API_ADMIN})
	NOW_SIGNING,

	@StatusInfo(
			value = 11,
			name = "В процессе открытия счёта",
			next = {},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_ADMIN, POS_CONSULTANT},
			editable = {
//	        		POS_BACK,
					POS_ADMIN})
	MANAGER_PROCESSING,

	@StatusInfo(
			value = 12,
			name = "В работе СБ",
			next = {9, 11, 15},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_ADMIN, POS_CONSULTANT},
			editable = {POS_ADMIN})
	SECURITY_PROCESSING,

	@StatusInfo(
			value = 13,
			name = "Выпуск сертификата",
			next = {20},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_ADMIN,
					POS_CONSULTANT},
			editable = {POS_ADMIN})
	ISSUING_CERT,

	@StatusInfo(
			value = 14,
			name = "На открытие",
			next = {9, 19, 27, 8},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_ADMIN, POS_CONSULTANT},
			editable = {
//	        		POS_BACK,
					POS_ADMIN})
	GO_OPEN,

	@StatusInfo(
			value = 15,
			name = "Отказ СБ",
			next = {},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_ADMIN_HUNTER,
					POS_FRONT, POS_ADMIN_HOME, POS_CONSULTANT},
			decline = true,
			editable = {POS_ADMIN})
	ERR_SECURITY_DECLINE,

	@StatusInfo(
			value = 16,
			name = "Отказ клиента",
			next = {1, 4},
			editable = {POS_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_OUTER_API_ADMIN})
	ERR_CLIENT_DECLINE,

	/**
	 * Остановлена обработка
	 */
	@StatusInfo(
			value = 17,
			name = "Автоматический отказ",
			next = {},
			decline = true,
			editable = {POS_ADMIN})
	ERR_AUTO_DECLINE,

	@StatusInfo(
			value = 18,
			name = "Отказ выездным специалистом",
			next = {},
			editable = {POS_ADMIN,
//					POS_FRONT_HUNTER, POS_ADMIN_HUNTER
			})
	ERR_MANAGER_DECLINE,

	@StatusInfo(
			value = 19,
			name = "Счет активирован",
			next = {13, 21},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_CONSULTANT},
			editable = {POS_ADMIN})
	GO_ACTIVE,

	/**
	 * Все, успешно открыт счет
	 */
	@StatusInfo(
			value = 20,
			name = "Счет успешно открыт",
			next = {28},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_CONSULTANT},
			editable = {POS_ADMIN})
	FULFILLED,

	@StatusInfo(
			value = 21,
			name = "Ошибка активации",
			next = {19},
			error = true,
			editable = {POS_ADMIN})
	ERR_CANT_ACTIVATE,

	@StatusInfo(
			value = 22,
			name = "Счет уже открыт в КУБ",
			next = {},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_ADMIN_HUNTER,
					POS_FRONT, POS_ADMIN_HOME, POS_CONSULTANT},
			decline = true,
			editable = {POS_ADMIN})
	ERR_ALREADY_OPENED,

	@StatusInfo(
			value = 24,
			name = "Недозвон",
			next = {7, 9},
			allowedTo = {POS_ADMIN, POS_FRONT,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_CONSULTANT},
			editable = {POS_FRONT, POS_ADMIN,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER
			})
	NO_ANSWER_DELIVERY,

	@StatusInfo(
			value = 25,
			name = "Встреча назначена",
			next = {9, 14, 16, 18, 24},
			allowedTo = {POS_ADMIN, POS_FRONT,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN, POS_CONSULTANT},
			editable = {POS_FRONT, POS_ADMIN,
//					POS_COURIER, POS_FRONT_HUNTER, POS_ADMIN_HUNTER
			})
	APPOINTMENT_MADE,

	@StatusInfo(
			value = 26,
			name = "Самозанятые",
			next = {},
			editable = {POS_ADMIN})
	SELF_EMPLOYED_APPLICATION,

	@StatusInfo(
			value = 27,
			name = "Отказ банком",
			next = {},
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_CONSULTANT},
			editable = {POS_ADMIN})
	BANK_REFUSED,

	@StatusInfo(
			value = 28,
			name = "Счёт закрыт",
			next = {},
			decline = true,
			allowedTo = {POS_ADMIN,
//					POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
					POS_ADMIN_HOME, POS_FRONT_HOME, POS_CONSULTANT},
			editable = {POS_ADMIN})
	CLOSED;

	/**
	 * Возвращает название статуса по русски
	 */
	private String text() {
		return Status.getAnnotation(this).name();
	}

	public static String decodeStatus(String s) {
		return Status.valueOf(s).text();
	}

	/**
	 * Возвращает аннотацию {StatusInfo}, связанную с этим членом enum
	 */
	private static StatusInfo getAnnotation(Status st) {
		Annotation[] nn = new Annotation[0];
		try {
			nn = Status.class.getField(st.name()).getAnnotations();
		} catch (NoSuchFieldException ex) {
			log.error(ex.getLocalizedMessage(), ex);
		}

		if (nn.length == 0) {
			throw new Error(String.format("Нужно аннотировать %s атрибутом @%s", st, StatusInfo.class.getName()));
		}
		return ((StatusInfo) nn[0]);
	}
}
