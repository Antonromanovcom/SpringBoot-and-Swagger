package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.statistics.ReportColumn;

@Getter
@Setter
public class AccountCount {
	@ReportColumn(title = "Источник клиента")
	public String source;
	@ReportColumn(title = "Количество созданных заявок")
	public Integer createdCount;
	@ReportColumn(title = "Количество открытых счетов")
	public Integer openedCount;
	@ReportColumn(title = "Доля активных счетов")
	public Double partOfActive;
}
