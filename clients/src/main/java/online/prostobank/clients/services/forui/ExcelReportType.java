package online.prostobank.clients.services.forui;

import com.google.common.collect.ImmutableList;
import io.vavr.Function3;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.services.ExcelGenerator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.Instant;
import java.util.List;

import static online.prostobank.clients.security.UserRolesConstants.*;

@Getter
@RequiredArgsConstructor
public enum ExcelReportType {
	REPORT_ALL("Все атрибуты клиента", ExcelGenerator::reportAll, ImmutableList.of(ROLE_POS_ADMIN, ROLE_SUPER_USER)),
	REPORT_ANALYSIS("Отчет для анализа лидов и клиентов", ExcelGenerator::reportAnalysis, ImmutableList.of(ROLE_POS_ADMIN)),
	REPORT_FLOW("Отчетность по движению", ExcelGenerator::reportFlow, ImmutableList.of(ROLE_POS_ADMIN)),
	REPORT_ACCOUNT_COUNT("Отчет по количеству счетов", ExcelGenerator::reportAccountCount, ImmutableList.of(ROLE_POS_ADMIN, ROLE_SUPER_USER)),
	TRANSFER_REPORT("Отчёт по переходам", ExcelGenerator::transferReport, ImmutableList.of(ROLE_POS_ADMIN)),
	SMART_ENGINE_USAGE("Использование SmartEngine", ExcelGenerator::smartEngineUsage, ImmutableList.of(ROLE_POS_ADMIN, ROLE_SUPER_USER)),
	COLD_REPORT("Выгрузка холодных лидов", ExcelGenerator::coldReport, ImmutableList.of(ROLE_POS_ADMIN, ROLE_POS_ADMIN_HOME)),
	;

	private final String ruName;
	private final Function3<ExcelGenerator, Instant, Instant, XSSFWorkbook> excelGeneratorFunction;
	private final List<String> roles;
}
