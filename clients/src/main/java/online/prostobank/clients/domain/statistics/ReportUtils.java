package online.prostobank.clients.domain.statistics;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static online.prostobank.clients.domain.OkvedConstants.SPECIAL_RISKY_OKVED;
import static online.prostobank.clients.utils.Utils.COMMA_DELIMITER;

@Slf4j
@UtilityClass
public class ReportUtils {
	public static String sqlFromFile(String resourceName) {
		try {
			return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
		} catch (IOException e) {
			log.error("Не удалось прочитать файл с sql-запросом");
			log.error(e.getLocalizedMessage(), e);
		}
		return StringUtils.EMPTY;
	}

	public static String getRiskyCodes(String okved, String blackOkved, String riskyOkved) {
		if (StringUtils.isEmpty(okved)) {
			return StringUtils.EMPTY;
		}
		Collection result = CollectionUtils.intersection(Arrays.asList(okved.split(COMMA_DELIMITER)), getBlackAndRiskyCodesAsList(blackOkved, riskyOkved));
		return result.isEmpty() ? StringUtils.EMPTY : StringUtils.join(result, COMMA_DELIMITER);

	}

	private static List<String> getBlackAndRiskyCodesAsList(String blackOkved, String riskyOkved) {
		List<String> result = new ArrayList<>();
		if (StringUtils.isNotEmpty(blackOkved)) {
			result.addAll(Arrays.asList(blackOkved.split(COMMA_DELIMITER)));
		}
		if (StringUtils.isNotEmpty(riskyOkved)) {
			result.addAll(Arrays.asList(riskyOkved.split(COMMA_DELIMITER)));
		}
		return result;
	}

	public static String getRiskyCodesString(Double scoringValueSource, String blackOkved, String riskyOkved) {
		if (StringUtils.isNotBlank(blackOkved)) {
			return "Есть запрещённые";
		} else if (!StringUtils.isNotBlank(riskyOkved)) {
			return (blackOkved == null && riskyOkved == null && scoringValueSource == 0.0D) ? "Не проверялся" : "Отсутствует";
		} else {
			List<String> intersection = new ArrayList<>();
			if (StringUtils.isNotBlank(riskyOkved)) {
				for (String code : riskyOkved.split(",")) {
					for (String riskOkved : SPECIAL_RISKY_OKVED) {
						if (code.startsWith(riskOkved)) {
							intersection.add(code);
						}
					}
				}
			}
			return CollectionUtils.isEmpty(intersection) ? "Есть рискованные" : StringUtils.join(intersection, ", ");
		}
	}
}
