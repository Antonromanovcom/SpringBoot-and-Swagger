package online.prostobank.clients.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Группировка типов документов
 */
@RequiredArgsConstructor
@Getter
public enum AttachmentMetaType {
	PASSPORT("Паспорт", "Паспорт представителя");

	private final String ruNameSP;
	private final String ruNameLLC;
}
