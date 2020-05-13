package online.prostobank.clients.services.dbo.service;

import org.springframework.http.HttpHeaders;

class DboHelper {
	private static final String APPLICATION_AVRO_JSON = "application/avro+json";

	static HttpHeaders getHttpHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_AVRO_JSON);
		headers.set("X-API-Version", "1");
		headers.set(HttpHeaders.ACCEPT, APPLICATION_AVRO_JSON);
		headers.setBearerAuth(accessToken);
		return headers;
	}
}
