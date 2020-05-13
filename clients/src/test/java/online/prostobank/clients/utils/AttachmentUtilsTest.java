package online.prostobank.clients.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static online.prostobank.clients.utils.AttachmentUtils.getNewFileNameWithIndex;
import static org.junit.Assert.assertEquals;

public class AttachmentUtilsTest {
	@Test
	public void testGetNewFileNameWithIndex() {
		String attachmentName00 = "sdgsgsgsg";
		String attachmentName01 = attachmentName00 + "(2)";
		String attachmentName1 = "243234";
		String attachmentName2 = "34234()";
		String attachmentName3 = "(252345626)";

		String ext = ".txt";
		String attachmentName50 = attachmentName00 + "(4)" + ext;
		String attachmentName51 = attachmentName00 + "(5)" + ext;
		String attachmentName6 = attachmentName1 + ext;
		String attachmentName7 = attachmentName2 + ext;
		String attachmentName8 = attachmentName3 + ext;
		String attachmentName9 = "test";

		List<String> existingNames = Arrays.asList(
				attachmentName00 + "(4)",
				attachmentName01,
				attachmentName1,
				attachmentName2,
				attachmentName3,

				attachmentName50,
				attachmentName51,
				attachmentName6,
				attachmentName7,
				attachmentName8,
				attachmentName9 + ext,
				attachmentName9 + "(1)" + ext
		);

		List<String> expected = Arrays.asList(
				attachmentName00 + "(5)", attachmentName00 + "(5)", attachmentName1 + "(1)", attachmentName2 + "(1)", attachmentName3 + "(1)",
				attachmentName00 + "(6)" + ext, attachmentName00 + "(6)" + ext, attachmentName1 + "(1)" + ext, attachmentName2 + "(1)" + ext, attachmentName3 + "(1)" + ext,
				attachmentName9 + "(2)" + ext
		);
//		System.out.println(String.format("%20s		%20s		%20s", "exist", "result", "expected"));
		IntStream.range(0, expected.size())
				.forEach(index -> {
					String existName = existingNames.get(index);
					String resultName = getNewFileNameWithIndex(existName, existingNames);
					String expectedName = expected.get(index);
//					System.out.println(String.format("%20s		%20s		%20s", existName, resultName, expectedName));
					assertEquals(expectedName, resultName);
				});
	}
}
