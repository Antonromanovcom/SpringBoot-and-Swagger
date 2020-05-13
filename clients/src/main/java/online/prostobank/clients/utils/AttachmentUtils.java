package online.prostobank.clients.utils;

import online.prostobank.clients.domain.Attachment;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class AttachmentUtils {
	public static final String WITH_NUMBER_ON_END = "(.+)(\\(\\d+\\))(\\.\\D{3,4})?$"; // example: "test(2)" or "test(2).txt"

	public static boolean isNotUniqueAttachmentName(String name, Collection<Attachment> exist) {
//		return exist.stream().anyMatch(existName -> Objects.equals(existName.getName(), name));
		return false;
	}

	public static String getUniqueName(String name, Set<Attachment> attachments) {
		/*if (isNotUniqueAttachmentName(name, attachments)) {
			List<String> names = attachments.stream().map(Attachment::getName).collect(Collectors.toList());
			return getNewFileNameWithIndex(name, names);
		} else {
			return name;
		}*/
		return name;
	}

	public static String getNewFileNameWithIndex(String name, Collection<String> names) { // have a test look
		String ext;
		String beforeExt;
		int lastIndexOfDot = name.lastIndexOf(".");
		if (lastIndexOfDot < 0) {
			ext = "";
			beforeExt = name;
		} else {
			ext = name.substring(lastIndexOfDot);
			beforeExt = name.substring(0, lastIndexOfDot);
		}

		String beforeNumber;
		boolean endWithNumber = name.matches(WITH_NUMBER_ON_END);
		if (endWithNumber) {
			beforeNumber = beforeExt.substring(0, beforeExt.lastIndexOf("("));
		} else {
			beforeNumber = beforeExt;
		}

		String finalBeforeNumber = beforeNumber;
		String existName = names.stream()
				.filter(existingName -> {
					if (lastIndexOfDot < 0) {
						return existingName.lastIndexOf(".") < 0 && existingName.startsWith(finalBeforeNumber);
					} else {
						return existingName.startsWith(finalBeforeNumber) && existingName.endsWith(ext);
					}
				})
                .max(Comparator.comparingInt(String::length).thenComparing(s -> s))
				.orElse(beforeNumber);

		int index = 1;
		if (existName.matches(WITH_NUMBER_ON_END)) {
			int lastIndexOfOpen = existName.lastIndexOf("(");
			int lastIndexOfClose = existName.lastIndexOf(")");
			String endNumber = existName.substring(lastIndexOfOpen + 1, lastIndexOfClose);
			index += Integer.parseInt(endNumber);
		}


		return beforeNumber + "(" + index + ")" + ext;
	}
}
