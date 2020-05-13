package online.prostobank.clients.services.email;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface EmailService {
	/**
	 * Send email with default
	 */
	void sendEmail(@Nullable File fileToAttach);

	void sendEmail(@Nullable File fileToAttach,
				   @Nonnull String emailSubject,
				   @Nonnull String messageText,
				   @Nonnull String sentTo);

	/**
	 * Send email excel with default
	 */
	void sendWorkBook(@Nonnull XSSFWorkbook myExcelBook);

	void sendWorkBook(@Nonnull XSSFWorkbook myExcelBook,
					  @Nonnull String emailSubject,
					  @Nonnull String messageText,
					  @Nonnull String sentTo);
}
