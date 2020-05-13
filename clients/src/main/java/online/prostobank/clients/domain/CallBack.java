package online.prostobank.clients.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.utils.Utils;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;

import static online.prostobank.clients.utils.Utils.DD_MM_YYYY_RU_FORMATTER;

@Getter
@NoArgsConstructor
public class CallBack {
	private LocalDate callbackDate;
	private LocalTime callbackTime;

	public @Nullable String getCallAtString() {
		if (callbackDate == null) {
			return null;
		}
		if (callbackTime == null) {
			return callbackDate.format(DD_MM_YYYY_RU_FORMATTER);
		}
		return callbackDate.format(DD_MM_YYYY_RU_FORMATTER)
				+ " в "
				+ callbackTime.format(Utils.TIME_FORMAT_MM_F);
	}
}
