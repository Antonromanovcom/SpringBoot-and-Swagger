package online.prostobank.clients.api.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.Contracts;
import online.prostobank.clients.domain.QuestionnaireValue;
import online.prostobank.clients.domain.enums.AccountantNoSignPermission;
import online.prostobank.clients.domain.enums.ContractTypes;
import online.prostobank.clients.domain.enums.TaxationType;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireDTO {
	// for now AccountApplication id
	private long clientId;

	private TaxationType taxationType;
	private Long monthlyRevenue;
	private String realCompanySize;
	private Boolean isChiefAccountantPresent;
	private AccountantNoSignPermission accountNoSignPermission;
	private String officialSite;
	private String registrationAddress;
	private String realLocationAddress;

	private String innOfSupplier;
	private String innOfConsumer;

	private String businessType;

	private Map<ContractTypes, Boolean> contractType;
	private String otherText;

	public static QuestionnaireDTO createFrom(AccountApplication application) {
		ClientValue client = application.getClient();
		QuestionnaireValue questionnaireValue = application.getQuestionnaireValue();
		Boolean isChiefAccountantPresent;
		if ("Да".equals(questionnaireValue.getIsChiefAccountantPresent())) {
			isChiefAccountantPresent = true;
		} else if ("Нет".equals(questionnaireValue.getIsChiefAccountantPresent())) {
			isChiefAccountantPresent = false;
		} else {
			isChiefAccountantPresent = null;
		}

		Contracts contractTypes = application.getContractTypes();
		Map<ContractTypes, Boolean> collect = Arrays.stream(ContractTypes.values())
				.collect(Collectors.toMap(
						Function.identity(),
						contractTypes1 -> contractTypes1.getGetter().apply(contractTypes))
				);
		return builder()
				.clientId(application.getId())
				.taxationType(TaxationType.valueBy(application.getTaxForm()))
				.monthlyRevenue(isNumeric(application.getIncome()) ? Long.parseLong(application.getIncome()) : null)
				.realCompanySize(questionnaireValue.getRealCompanySize())
				.isChiefAccountantPresent(isChiefAccountantPresent)
				.accountNoSignPermission(AccountantNoSignPermission.valueBy(questionnaireValue.getAccountNoSignPermission()))
				.officialSite(questionnaireValue.getOfficialSite())
				.registrationAddress(client.getAddress())
				.realLocationAddress(client.getResidentAddress())
				.innOfSupplier(application.getContragents())
				.innOfConsumer(application.getContragentsRecip())
				.businessType(questionnaireValue.getBusinessType())
				.contractType(collect)
				.otherText(contractTypes.getOtherText())
				.build();
	}
}
