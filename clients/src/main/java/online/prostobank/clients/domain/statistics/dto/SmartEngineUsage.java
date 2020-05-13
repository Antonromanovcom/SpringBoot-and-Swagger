package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.statistics.ReportColumn;

/**
 * DTO для формирования отчета об использовании функционала распознавания сканов документов
 */
@Getter
@Setter
public class SmartEngineUsage {
	@ReportColumn(title = "Пользователь")
	public String username;
	@ReportColumn(title = "Тип документа")
	public String functionalType;
	@ReportColumn(title = "Количество использований")
	public Integer count;
}
