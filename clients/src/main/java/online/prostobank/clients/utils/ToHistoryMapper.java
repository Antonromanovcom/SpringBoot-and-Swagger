package online.prostobank.clients.utils;

import online.prostobank.clients.api.dto.HistoryField;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ToHistoryMapper {
	private static final String NO_DATA = "<не указано>";
	public static String toHistory(Object value) {
		if (value == null) {
			return "";
		}
		List<Field> annotatedFields = Arrays.stream(value.getClass().getDeclaredFields())
				.filter(it -> it.getAnnotation(HistoryField.class) != null)
				.collect(Collectors.toList());

		if (annotatedFields.isEmpty()) {
			return String.valueOf(value);
		}

		return annotatedFields.stream()
				.map(field -> {
					Class<?> fieldType = field.getType();
					String historyMessage = "";
					field.setAccessible(true);
					Object fieldValue = null;

					try {
						fieldValue = field.get(value);
					} catch (Exception ex) {
						//do nothing
					}

					if (fieldValue != null) {
						if (fieldType.isPrimitive()) {
							historyMessage = String.valueOf(fieldValue);
						} else if (fieldType.equals(List.class)) {
							if (((List) fieldValue).isEmpty()) {
								historyMessage = NO_DATA;
							} else {
								for (Object subValue : (List) fieldValue) {
									historyMessage += toHistory(subValue);
								}
							}

						} else {
							historyMessage = toHistory(fieldValue);
						}
					} else {
						historyMessage = NO_DATA;
					}
					historyMessage = StringUtils.isEmpty(historyMessage) ? NO_DATA : historyMessage;
					return field.getAnnotation(HistoryField.class).title() + " : { " + historyMessage + " }";
				})
				.collect(Collectors.joining(", "));
	}
}
