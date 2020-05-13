package online.prostobank.clients.domain.tss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TssStatus {
	NEW("Новая запись в очереди на подписание сообщения"),
	IN_PROGRESS("Заявка на подписание отправлена в ядро работы с КриптоПро"),
	NEED_CONFIRM("Необходимо подтверждение подписания"),
	REJECTED("Пользователь отменил подписание"),
	FAILED_CONFIRM("Пользователь не подтвердил подписание (по времени или ошибка подтверждения)"),
	CONFIRMED("Пользователь подтвердил подписание"),
	SUCCESS("Документ подписан, подтвержден"),
	FAILED("Произошла ошибка во время подписания документа"),
	;
	private final String ruDesc;
}
