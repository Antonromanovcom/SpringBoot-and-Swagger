package online.prostobank.clients.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum AttachmentFunctionalType {
	UNKNOWN("", "", "", Optional.empty(), false),
	PASSPORT_FIRST("passport_first", "Паспорт (первая страница)", "Паспорт представителя (первая страница)", Optional.of(AttachmentMetaType.PASSPORT), true),
	PASSPORT_SECOND("passport_second", "Паспорт (страница регистрации)", "Паспорт представителя (страница регистрации)", Optional.of(AttachmentMetaType.PASSPORT), true),
	SNILS("snils", "СНИЛС", "СНИЛС представителя", Optional.empty(), true),
	FIN_REPORTS("fin_reports", "Документы фин. отчетности, (если уже отчитывались в ИФНС)", "Документы фин. отчетности, (если уже отчитывались в ИФНС)", Optional.empty(), false),
	LICENSES("licenses", "Лицензии и/или патент, (если есть)", "Лицензии, (если есть)", Optional.empty(), false),
	BUSINESS_INFO("business_info", "Сведения о бизнесе", "Сведения о бизнесе", Optional.empty(), false),
	ORGANIZATION_CHART("articles", "", "Устав со всеми изменениями", Optional.empty(), false),
	LEAD_ARTICE("lead_article", "", "Решение о полномочиях руководителя, (с печатью)", Optional.empty(), false),
	BENEFICIARY_DOCS("beneficiars_docs", "", "Паспорта бенефициаров, (если возможно предоставить оригиналы)", Optional.empty(), false),
	ADDRESS_CONFIRMATION("address_confirmation", "", "Подтверждение юр. адреса", Optional.empty(), false),
	INN("inn", "ИНН", "ИНН", Optional.empty(), true),
	RENT_AGREEMENT("rent_agreement", "Договор аренды", "Договор аренды", Optional.empty(), false),
	OTHER("other", "Другие документы", "Другие документы", Optional.empty(), false),
	;

	private final String frontendKey;
	private final String ruNameSP;
	private final String ruNameLLC;
	private final Optional<AttachmentMetaType> metaType; //группа, в которую входит тип документа (для построения иерархии в UI)
	private final boolean isUnique; //актуальный документ такого типа может существовать в единственном экземпляре (например, паспорт)

	public String getRuName(ClientType clientType) {
		return Optional.ofNullable(clientType)
				.map(type -> {
					switch (type) {
						case SP:
							return ruNameSP;
						case LLC:
							return ruNameLLC;
						default:
							return null;
					}
				})
				.orElse("");
	}

	public static AttachmentFunctionalType getByStringKey(String key) {
		return Arrays.stream(values())
				.filter(type -> Objects.equals(key, type.getFrontendKey()) || Objects.equals(key, type.name()))
				.map(type -> {
					if (type == UNKNOWN) {
						return OTHER;
					}
					return type;
				})
				.findFirst()
				.orElse(OTHER);
	}

	/**
	 * Тип документа позволяет автоматическое распознание
	 */
	public boolean isRecognizable() {
		return this == PASSPORT_FIRST
				|| this == SNILS
				|| this == INN;
	}

	/**
	 * Типы документов, попадающие в указанную группу
	 * @param group
	 * @return
	 */
	public static List<AttachmentFunctionalType> getTypesByGroup(AttachmentMetaType group) {
		return Arrays.stream(AttachmentFunctionalType.values())
				.filter(it -> it.getMetaType().isPresent())
				.filter(it -> it.getMetaType().get().equals(group))
				.collect(Collectors.toList());
	}

	/**
	 * Типы документов, не входящие ни в какую группу
	 * @return
	 */
	public static List<AttachmentFunctionalType> getUngroupedTypes() {
		return Arrays.stream(AttachmentFunctionalType.values())
				.filter(it -> !it.getMetaType().isPresent())
				.collect(Collectors.toList());
	}
}
