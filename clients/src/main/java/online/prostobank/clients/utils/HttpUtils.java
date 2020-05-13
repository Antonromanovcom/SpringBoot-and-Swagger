package online.prostobank.clients.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class HttpUtils {
	public static boolean is2xxSuccessful(int code) {
		return code / 100 == 2;
	}

	public static HttpHeaders setBearerAuth(HttpHeaders headers, String token) {
		headers.setBearerAuth(token);
		return headers;
	}
}
