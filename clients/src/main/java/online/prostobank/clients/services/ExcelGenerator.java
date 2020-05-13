package online.prostobank.clients.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.ExcelGeneratorProperties;
import online.prostobank.clients.domain.client.ClientKeycloak;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.status_log.StatusHistoryRepository;
import online.prostobank.clients.domain.statistics.ReportColumn;
import online.prostobank.clients.domain.statistics.StatisticsRepository;
import online.prostobank.clients.domain.statistics.dto.*;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import online.prostobank.clients.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getSourceForColdReport;
import static online.prostobank.clients.services.forui.ExcelReportType.*;
import static online.prostobank.clients.utils.Utils.COMMA_DELIMITER;
import static online.prostobank.clients.utils.Utils.DATE_TIME_RU_FORMATTER;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExcelGenerator {
    private final AccountApplicationRepository accountApplicationRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final StatisticsRepository statisticsRepository;
    private final ExcelGeneratorProperties config;
    private final StatusHistoryRepository statusHistoryRepository;

    public XSSFWorkbook createKeycloakWorkBook(List<ClientKeycloak> list) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Выгрузка Keycloak");
        // номер строки в документе
        int rowNum = 0;
        Row rowHeader = sheet.createRow(rowNum++);
        //заголовки столбцов
        addCell(rowHeader, 0, "client_id");
        addCell(rowHeader, 1, "email_login");
        addCell(rowHeader, 2, "id_in_keycloak");
        addCell(rowHeader, 3, "created_at");

        for (ClientKeycloak clientKeycloak : list) {
            Long clientId = clientKeycloak.getClientId();
            String login = clientKeycloak.getLogin();
            String idInKeycloak = clientKeycloak.getKeycloakId().toString();
            Instant createdAt = clientKeycloak.getCreatedAt();

            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, clientId.toString());
            addCell(row, 1, login);
            addCell(row, 2, idInKeycloak);
            addCell(row, 3, DATE_TIME_RU_FORMATTER.format(createdAt));
        }

        autoSizeColumns(workbook);
        return workbook;
    }

    public XSSFWorkbook reportAll(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<AllAttributes> data = statisticsRepository.allAttributes(startDate, endDate.plus(1, ChronoUnit.DAYS));

        return createWorkbook(data, REPORT_ALL.getRuName(), AllAttributes.class, true);
    }

    public XSSFWorkbook reportAnalysis(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<AnalysisAttributes> data = statisticsRepository.analysisAttributes(startDate, endDate.plus(1, ChronoUnit.DAYS));

        return createWorkbook(data, REPORT_ANALYSIS.getRuName(), AnalysisAttributes.class, true);
    }

    public XSSFWorkbook reportFlow(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<AppFlow> data = accountApplicationRepository
                .findAllByDateIntervalAndActiveIsTrue(startDate, endDate.plus(1, ChronoUnit.DAYS))
                .stream()
                .flatMap(application -> AppFlow.createFrom(application, config.getAppUrl(), statusHistoryRepository.selectAllByClientIdOrdered(application.getId())).stream())
                .collect(Collectors.toList());
        return createWorkbook(data, REPORT_FLOW.getRuName(), AppFlow.class, true);
    }

    public XSSFWorkbook smartEngineUsage(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<SmartEngineUsage> data = statisticsRepository.smartEngineUsage(startDate, endDate.plus(1, ChronoUnit.DAYS));

        return createWorkbook(data, SMART_ENGINE_USAGE.getRuName(), SmartEngineUsage.class, false);
    }

    public XSSFWorkbook coldReport(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<String> source = getSourceForColdReport().orElseThrow(() -> new IllegalArgumentException("Source not found"));
        List<ColdStats> data = statisticsRepository.coldStats(startDate, endDate.plus(1, ChronoUnit.DAYS), source);
        data.forEach(s -> keycloakAdminClient.fetchRolesByUsername(s.operator)
                .ifPresent(roles -> s.operatorRole = StringUtils.join(roles, COMMA_DELIMITER)));

        return createWorkbook(data, COLD_REPORT.getRuName(), ColdStats.class, false);
    }

    public XSSFWorkbook reportAccountCount(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<AccountCount> data = statisticsRepository.accountCount(startDate, endDate.plus(1, ChronoUnit.DAYS));
        return createWorkbook(data, REPORT_ACCOUNT_COUNT.getRuName(), AccountCount.class, false);
    }

    public XSSFWorkbook transferReport(@NotNull Instant startDate, @NotNull Instant endDate) {
        List<TransferStats> data = TransferStats.transferReport(
                accountApplicationRepository.findAllByDateIntervalAndActiveIsTrue(startDate, endDate.plus(1, ChronoUnit.DAYS)),
                statusHistoryRepository
        );
        return createWorkbook(data, TRANSFER_REPORT.getRuName(), TransferStats.class, true);
    }

    private XSSFWorkbook createWorkbook(List<?> data, String ruName, Class<?> dataClass, boolean needTime) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(ruName);
        Row rowHeader = sheet.createRow(0);

        //выбираем поля с аннотациями (только они попадают в отчет)
        List<Field> annotatedFields = Arrays.stream(dataClass.getFields())
                .filter(it -> it.getAnnotation(ReportColumn.class) != null)
                .sorted((o1, o2) -> {
                    Class<?> aClass1 = o1.getDeclaringClass();
                    Class<?> aClass2 = o2.getDeclaringClass();
                    if (aClass1 == aClass2) {
                        return 0;
                    } else {
                        if (aClass1.isAssignableFrom(aClass2)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                })
                .collect(Collectors.toList());

        //заголовки столбцов
        for (int i = 0; i < annotatedFields.size(); i++) {
            addCell(rowHeader, i, annotatedFields.get(i).getAnnotation(ReportColumn.class).title());
        }

        CellStyle dateCellStyle = null;
        CellStyle dateTimeCellStyle = null;
        CellStyle timeCellStyle = null;
        if (needTime) {
            dateCellStyle = getCellStyle(Utils.DD_MM_YYYY, workbook);
            dateTimeCellStyle = getCellStyle(Utils.DATE_TIME_FORMAT, workbook);
            timeCellStyle = getCellStyle(Utils.TIME_FORMAT, workbook);
        }

        addRows(sheet, annotatedFields, data, dateCellStyle, dateTimeCellStyle, timeCellStyle);

        autoSizeColumns(workbook);
        return workbook;
    }

    private void addRows(XSSFSheet sheet, List<Field> annotatedFields, List<?> data, CellStyle dateCellStyle, CellStyle dateTimeCellStyle, CellStyle timeCellStyle) {
        int rowNum = 0;
        for (Object dataRow : data) {
            Row row = sheet.createRow(++rowNum);
            for (int i = 0; i < annotatedFields.size(); i++) {
                ReportColumn annotation = annotatedFields.get(i).getAnnotation(ReportColumn.class);
                try {
                    Object value = annotatedFields.get(i).get(dataRow);
                    if (value == null) {
                        addCell(row, i, StringUtils.EMPTY);
                    } else {
                        switch (annotation.type()) {
                            case STRING:
                                addCell(row, i, String.valueOf(value));
                                break;
                            case DATE:
                                addCell(row, i, (Instant) value, dateCellStyle);
                                break;
                            case TIME:
                                addCell(row, i, (Instant) value, timeCellStyle);
                                break;
                            case DATE_TIME:
                                addCell(row, i, (Instant) value, dateTimeCellStyle);
                                break;
                            default:
                                addCell(row, i, StringUtils.EMPTY);
                        }
                    }
                } catch (Exception ex) {
                    addCell(row, i, StringUtils.EMPTY);
                }
            }
        }
    }

    private CellStyle getCellStyle(String format, XSSFWorkbook wb) {
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
        return cellStyle;
    }

    private void addCell(Row row, int colNum, String value) {
        try {
            Cell cell = row.createCell(colNum);
            if (StringUtils.isBlank(value)) {
                value = "-";
            }
            cell.setCellValue(value);
        } catch (Exception e) {
            log.error("ошибка добавления ячейки", e);
        }
    }

    private void addCell(Row row, int colNum, Instant value, CellStyle style) {
        try {
            Cell cell = row.createCell(colNum);

            if (value == null) {
                cell.setCellValue("-");
                return;
            }
            cell.setCellValue(Date.from(value));

            if (style != null) {
                cell.setCellStyle(style);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void autoSizeColumns(XSSFWorkbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    sheet.autoSizeColumn(columnIndex);
                }
            }
        }
    }
}
