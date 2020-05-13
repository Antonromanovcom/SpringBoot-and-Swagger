package online.prostobank.clients.services.forui;

import java.time.Instant;
import java.util.Optional;

public interface ExcelService {
	/**
	 * Выгрузки excel
	 *
	 * @param type - тип выгрузки
	 * @param from - дата с
	 * @param to   - дата по
	 * @return - excel контент
	 */
	Optional<byte[]> createWorkbook(ExcelReportType type, Instant from, Instant to);
}
