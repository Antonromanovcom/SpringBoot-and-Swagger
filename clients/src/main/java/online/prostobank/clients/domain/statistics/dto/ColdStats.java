package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.statistics.ReportColumn;

/**
 * DTO для формирования excel-отчета "Выгрузка холодных лидов за период"
 */
@Getter
@Setter
public class ColdStats {
	@ReportColumn(title = "Источник")
	public String source;
	@ReportColumn(title = "Роль")
	public String operatorRole;
	@ReportColumn(title = "Оператор")
	public String operator;
	@ReportColumn(title = "Создатель")
	public String creator;

	@ReportColumn(title = "Создан")
	public Long newClient;
	@ReportColumn(title = "Информация подтверждена")
	public Long contactInfoConfirmed;
	@ReportColumn(title = "Недозвон")
	public Long noAnswer;
	@ReportColumn(title = "Проверка лида")
	public Long checkLead;
	@ReportColumn(title = "Отказ клиента")
	public Long clientDeclined;
	@ReportColumn(title = "Ожидание документов")
	public Long waitForDocs;
	@ReportColumn(title = "Документы вложены")
	public Long documentsExists;
	@ReportColumn(title = "Дозапрос документов")
	public Long requiredDocs;
	@ReportColumn(title = "В процессе открытия счёта")
	public Long managerProcessing;
	@ReportColumn(title = "Счет открыт")
	public Long activeClient;
	@ReportColumn(title = "Счет закрыт")
	public Long inactiveClient;
	@ReportColumn(title = "Автоматический отказ")
	public Long autoDeclined;
}
