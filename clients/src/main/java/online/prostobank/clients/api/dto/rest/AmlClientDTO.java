package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.api.dto.client_detail.BeneficiaryDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.TaxationType;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Value
@Builder
public class AmlClientDTO {
	private String name;                        // Наименование ЮЛ
	private String shortName;                   // Короткое наименование ЮЛ
	private String accountNumber;               // Номер счёта
	private String regPlaceAddress;             // Адрес регистрации
	private String currentPlaceAddress;         // Адрес фактического проживания
	private LocalDate regDate;                  // Дата регистрации
	private String head;                        // ФИО руководителя
	private LocalDate headDob;                  // Дата рождения
	private String primaryCode;                 // Основной вид деятельности - оквэд
	private String businessType;                // Основной вид деятельности - заполненное менеджером
	private List<BeneficiaryDTO> beneficiary;   // Бенефициарный владелец ФИО
	private String email;                       // email
	private String phone;                       // телефон
	private TaxationType taxationType;          // Система налогообложения
	private String realCompanySize;             // Штатная численность
	private Boolean isChiefAccountantPresent;   // Наличие главного бухгалтера
	private String officialSite;                // Официальный сайт
	private String inn;                         // ИНН

	public static AmlClientDTOBuilder createFrom(AccountApplication accountApplication) {
		Boolean isChiefAccountantPresent = null;
		if ("Да".equals(accountApplication.getQuestionnaireValue().getIsChiefAccountantPresent())) {
			isChiefAccountantPresent = true;
		} else if ("Нет".equals(accountApplication.getQuestionnaireValue().getIsChiefAccountantPresent())) {
			isChiefAccountantPresent = false;
		}
		return builder().
				name(accountApplication.getClient().getName())
				.shortName(accountApplication.getClient().getShortName())
				.accountNumber(accountApplication.getAccount().getAccountNumber())
				.regPlaceAddress(accountApplication.getClient().getAddress())
				.currentPlaceAddress(accountApplication.getClient().getResidentAddress())
				.regDate(accountApplication.getClient().getRegDate())
				.head(accountApplication.getClient().getHead())
				.headDob(accountApplication.getPerson().getDob())
				.primaryCode(accountApplication.getClient().getPrimaryCodes())
				.businessType(accountApplication.getQuestionnaireValue().getBusinessType())
				.email(accountApplication.getClient().getEmail())
				.phone(accountApplication.getClient().getPhone())
				.taxationType(TaxationType.valueBy(accountApplication.getTaxForm()))
				.realCompanySize(accountApplication.getQuestionnaireValue().getRealCompanySize())
				.isChiefAccountantPresent(isChiefAccountantPresent)
				.officialSite(accountApplication.getQuestionnaireValue().getOfficialSite())
				.inn(accountApplication.getClient().getInn());
	}
}
