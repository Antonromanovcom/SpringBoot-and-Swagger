package online.prostobank.clients.api.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ChecksResultValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value
@RequiredArgsConstructor
public class ChecksDTOExtended {

	@JsonProperty(value = "results")
	private final List<Check> results;
	@JsonProperty(value = "available_check_types")
	private List<CheckType> availableChecks = Arrays.asList(CheckType.values());
	@JsonProperty(value = "available_check_results")
	private List<CheckResult> availableResults = Arrays.asList(CheckResult.values());

	public static ChecksDTOExtended createFrom(AccountApplication application) {
		ChecksResultValue checkValues = application.getChecks();
		List<Check> checks = new ArrayList<>();
		checks.add(new Check(CheckType.ARRESTS, getResult(checkValues.getArrestsFns()), checkValues.getArrestsFns()));
		checks.add(new Check(CheckType.PASSPORT, getResult(checkValues.getPassportCheck()), checkValues.getPassportCheck()));
		checks.add(new Check(CheckType.P550_CEO, getResult(checkValues.getP550checkHead()), checkValues.getP550checkHead()));
		checks.add(new Check(CheckType.P550_COMPANY, getResult(checkValues.getP550check()), checkValues.getP550check()));
		checks.add(new Check(CheckType.P550_FOUNDER, getResult(checkValues.getP550checkFounder()), checkValues.getP550checkFounder()));
		checks.add(new Check(CheckType.SCORING, getScoringResult(application, checkValues.getKonturCheck()), String.valueOf(checkValues.getKonturCheck())));
		return new ChecksDTOExtended(checks);
	}

	@JsonIgnore
	public String getChecksAsString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException ex) {
			return "Не удалось получить данные о проверках";
		}
	}

	private static CheckResult getResult(String value) {
		if (StringUtils.isEmpty(value)) {
			return CheckResult.NO_DATA;
		} else if (ChecksResultValue.OK.equals(value)) {
			return CheckResult.SUCCESS;
		} else {
			return CheckResult.FAIL;
		}
	}

	private static CheckResult getScoringResult(AccountApplication application, Double value) {
		if (value == null) {
			return CheckResult.NO_DATA;
		} else if (value.compareTo(application.getAllowedScoring()) < 0) {
			return CheckResult.SUCCESS;
		} else {
			return CheckResult.FAIL;
		}
	}

	private enum CheckType {
		SCORING,
		ARRESTS,
		PASSPORT,
		P550_COMPANY,
		P550_CEO,
		P550_FOUNDER
	}

	private enum CheckResult {
		NO_DATA,
		SUCCESS,
		FAIL
	}

	@AllArgsConstructor
	private static class Check {
		@JsonProperty(value = "check_type")
		private CheckType type;
		@JsonProperty(value = "check_result")
		private CheckResult result;
		@JsonProperty(value = "check_message")
		private String message;
	}
}
