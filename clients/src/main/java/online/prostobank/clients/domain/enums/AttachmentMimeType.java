package online.prostobank.clients.domain.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author yv
 */
public enum AttachmentMimeType {
	IMAGE,
	PDF,
	DOCUMENT,
	OTHER;

	public static final List<String> IMAGE_TYPES = Arrays.asList(
			"image/jpeg",
			"image/x-citrix-jpeg",
			"image/pjpeg",
			"image/png",
			"image/x-citrix-png",
			"image/x-png",
			"image/tiff"
	);
	public static final List<String> DOCUMENT_TYPES = Arrays.asList(
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/msword",
			"application/vnd.ms-word.document.macroenabled.12"
	);
	public static final List<String> PDF_TYPES = Collections.singletonList(
			"application/pdf"
	);

	public static AttachmentMimeType guessFromMime(String fileType) {
		if (IMAGE_TYPES.contains(fileType)) {
			return IMAGE;
		} else if (PDF_TYPES.contains(fileType)) {
			return PDF;
		} else if (DOCUMENT_TYPES.contains(fileType)) {
			return DOCUMENT;
		} else {
			return OTHER;
		}
	}
}
