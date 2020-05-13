package online.prostobank.clients.services.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ClientAttachmentUtils {
	public static byte[] toZip(Map<String, byte[]> attachments) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zipFile = new ZipOutputStream(bos)) {
				attachments.forEach((attachmentName, binaryContent) -> {
					try {
						ZipEntry zipentry = new ZipEntry(attachmentName);
						zipFile.putNextEntry(zipentry);
						zipFile.write(binaryContent);
					} catch (IOException ex) {
						log.info("error adding file " + attachmentName + " to zip archive");
					}
				});
			}
			return bos.toByteArray();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return new byte[0];
	}

	public static ResponseEntity<InputStreamResource> getStreamResponse(byte[] bytes, String fileName, boolean needOctetStream) {
		HttpHeaders headers = new HttpHeaders();
		if (needOctetStream) {
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		}
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
		InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(bytes));
		return new ResponseEntity<>(isr, headers, HttpStatus.OK);
	}

	public static String encode(String filename) {
		if (filename == null) {
			return "";
		}
		try {
			return "attachment;filename*=UTF-8\' \'" + URLEncoder.encode(filename, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return filename;
		}
	}

	public static boolean checkFileName(String fileName) {
		OSValidator osValidator = new OSValidator();
		//. and .. have special meaning on all platforms
		if (fileName.equals(".") || fileName.equals("..")) return false;

		if (osValidator.isOSWin()) {
			//empty names are not valid
			final int length = fileName.length();
			if ((length == 0) || (length > 259)) return false;
			final char lastChar = fileName.charAt(length - 1);
			// filenames ending in dot are not valid
			if (lastChar == '.') return false;
			// file names ending with whitespace are truncated (bug 118997)
			if (Character.isWhitespace(lastChar)) return false;
			int dot = fileName.indexOf('.');
			//on windows, filename suffixes are not relevant to name validity
			String basename = dot == -1 ? fileName : fileName.substring(0, dot);

			if (checkInvalidChars(basename.toLowerCase(), osValidator.isOSWin())) {
				return true;
			}

			if (checkInvalidChars(fileName, osValidator.isOSWin())) {
				return true;
			}
		} else {
			if (checkInvalidChars(fileName, osValidator.isOSWin())) {
				return true;
			}
		}
		return true;
	}

	private static boolean checkInvalidChars(String fileName, boolean isWindows) {
		char[] INVALID_RESOURCE_CHARACTERS;
		final String[] INVALID_RESOURCE_BASENAMES;

		if (isWindows) {

			INVALID_RESOURCE_CHARACTERS = new char[]{'\\', '/', ':', '*', '?', '"', '<', '>', '|'};
			INVALID_RESOURCE_BASENAMES = new String[]{"aux", "com1", "com2", "com3", "com4",
					"com5", "com6", "com7", "com8", "com9", "con", "lpt1", "lpt2",
					"lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "nul", "prn"};
			Arrays.sort(INVALID_RESOURCE_BASENAMES);

			if (Arrays.binarySearch(INVALID_RESOURCE_BASENAMES, fileName) >= 0)
				return false;
		} else {

			INVALID_RESOURCE_CHARACTERS = new char[]{'/', '\0',};
		}

		for (char invalid_resource_character : INVALID_RESOURCE_CHARACTERS) {
			if (fileName.indexOf(invalid_resource_character) != -1) {
				return false;
			}
		}
		return true;
	}

}
