package online.prostobank.clients.services.forui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.services.ExcelGenerator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {
	private final ExcelGenerator excelGenerator;

	@Override
	public Optional<byte[]> createWorkbook(ExcelReportType type, Instant from, Instant to) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			XSSFWorkbook workbook = type.getExcelGeneratorFunction().apply(excelGenerator, from, to);
			workbook.write(outputStream);
			workbook.close();
			return Optional.ofNullable(outputStream.toByteArray());
		} catch (Exception ex) {
			log.error("error on excel stream", ex);
			return Optional.empty();
		}
	}
}
