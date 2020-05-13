package online.prostobank.clients.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.Attachment;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
public class Utils {
	public static String UNKNOWN_CITY = "Неизвестно";

	public static final String[] FILE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf", ".tiff", ".tif", ".docx", ".doc", ".p7s"};

	public static final String COMMA_DELIMITER = ",";
	public static final String COMMA_DELIMITER_W_SPACE = ", ";
	public static final String TILDE_DELIMITER = "~";
	public static final String ROBOT = "Робот";

	public static final String DD_MM_YYYY_RU = "dd.MM.yyyy";
	public static final String DD_MM_YYYY = "dd-MM-yyyy";
	public static final String ON_DD_MM_YYYY = "на dd.MM.yyyy";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";

	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String TIME_FORMAT_MM = "HH:mm";

	public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static final String DATE_TIME_FORMAT_RU = "dd.MM.YYYY HH:mm";

	public static final String DD_MM_YYYY_V_HH_MM = "dd.MM.YYYY в HH:mm";
	public static final String DD_MM_YYYY_V_HH_MM_SS = "dd.MM.YYYY в HH:mm:ss";

	public static final String YYYY_MM_DD_HH_MM_SS = "YYYY-MM-dd HH:mm:ss";
	public static final String D_MMM_YYYY_HH_MM = "d MMM yyyy 'в' HH:mm";
	public static final String YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final DateTimeFormatter DD_MM_YYYY_RU_FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY_RU)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY)
			.withZone(ZoneId.systemDefault());

	public static final DateTimeFormatter TIME_FORMAT_MM_F = DateTimeFormatter.ofPattern(TIME_FORMAT_MM)
			.withZone(ZoneId.systemDefault());

	public static final DateTimeFormatter DATE_TIME_RU_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_RU)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter RU_FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY_V_HH_MM)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter V_FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY_V_HH_MM_SS)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter D_MMM_YYYY_HH_MM_F = DateTimeFormatter.ofPattern(D_MMM_YYYY_HH_MM)
			.withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter ON_DD_MM_YYYY_F = DateTimeFormatter.ofPattern(ON_DD_MM_YYYY)
			.withZone(ZoneId.systemDefault());

	public static final Tika TIKA = new Tika();

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";


	public static String toStringOrNull(Object src) {
		return src == null ? null : src.toString();
	}

	public static String parseOrNull(LocalDate date) {
		return date == null ? null : date.format(DD_MM_YYYY_RU_FORMATTER);
	}

	public static String stringOrNull(String src) {
		return isEmpty(src) ? null : src;
	}

	public static LocalDate getDateFromStringEndAfterSpace(String withDate) {
		if (withDate == null) return null;
		int indexOf = withDate.lastIndexOf(" ");
		if (indexOf < 0) return null;
		try {
			return LocalDate.parse(withDate.substring(indexOf + 1), DD_MM_YYYY_RU_FORMATTER);
		} catch (Exception e) {
			log.warn("Can't parse dates. {}", e.getMessage());
			return null;
		}
	}

	public static String getAccountLink(String lkUrl, String loginUrl) {
		return lkUrl + loginUrl + "/documents/";
	}

	public static Instant toInstant(LocalDate date) {
		return date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
	}

	public static String getMimeType(byte[] data) {
		return TIKA.detect(data);
	}

	public static String getMimeTypeByFileName(String fileName) {
		return TIKA.detect(fileName);
	}

	public static String wrapProperty(String property) {
		return Optional.ofNullable(property).orElse(StringUtils.EMPTY);
	}

	public static String getRandomString(int n) {

		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			sb.append(ALPHA_NUMERIC_STRING.charAt((int) (ALPHA_NUMERIC_STRING.length() * Math.random())));
		}

		return sb.toString();
	}

	public static String addFourRandomCharToAttachName(Attachment attachment) {
		String rnd = "_" + getRandomString(4);
		String name = attachment == null ? "" : attachment.getAttachmentName();
		if (name.contains(".")) {
			return name.replaceFirst("\\.", rnd + ".");
		}
		return name + rnd;
	}

	public static <T> String toJson(T obj) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try {
			return ow.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("can't convert object to json: " + e);
		}
	}

	public static String dateFormat(TemporalAccessor instant, String pattern) {
		if (instant == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
		return formatter.format(instant);
	}

	/**
	 * Определение является ли обладатель указанных ИНН и ОГРН юрлицом
	 */
	public static boolean isLegalEntity(String inn, String ogrn) {
		return !((StringUtils.isNotBlank(ogrn) && ogrn.length() == TaxNumberUtils.IP_OGRN_LENGTH)
				|| (StringUtils.isNotBlank(inn) && inn.length() == TaxNumberUtils.IP_INN_LENGTH));

	}

	/**
	 * Получение  расширения имени файла (часть после точки, если присутствует)
	 */
	public static String getFileNameExtension(String name) {
		if (StringUtils.isEmpty(name)) {
			return "";
		}
		if (name.contains(".")) {
			String[] parts = name.split("\\.");
			return "." + parts[Math.max(0, parts.length - 1)];
		}
		return "";
	}

	public static <T, R> R valueOrDefault(T src, Function<T, R> getter, R def) {
		if (src == null) {
			return def;
		}
		R value = getter.apply(src);
		return value == null ? def : value;
	}
}
