package online.prostobank.clients.interceptors;

import online.prostobank.clients.domain.LoggingRecord;
import online.prostobank.clients.domain.repository.LoggingRecordRepository;
import online.prostobank.clients.utils.NonEmptyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public abstract class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

	@Autowired
	private LoggingRecordRepository loggingRecordRepository;

	private static final Logger LOG = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

	void traceRequest(HttpRequest request, byte[] body, LoggingRecord loggingRecord) {
		loggingRecord.setRequestTime(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
		loggingRecord.setReqResPair(UUID.randomUUID().toString());
		LOG.debug("=========================== request for " + loggingRecord.getReqResPair() + " begin ================================================");
		LOG.debug("URI         : {}", request.getURI());
		LOG.debug("Method      : {}", request.getMethod());
		LOG.debug("Headers     : {}", request.getHeaders());
		LOG.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
		LOG.debug("========================== request for " + loggingRecord.getReqResPair() + " end ================================================");

		loggingRecord.setPath(request.getURI().getPath());
		loggingRecord.setMethod(request.getMethodValue());
		loggingRecord.setRequestHeaders(request.getHeaders().toString());
		loggingRecord.setRequestBody(new String(body, StandardCharsets.UTF_8));
		loggingRecord.setArguments(request.getURI().getQuery());
		loggingRecordRepository.save(loggingRecord);
	}

	void traceResponse(ClientHttpResponse response, LoggingRecord loggingRecord) throws IOException {
		loggingRecord.setResponseTime(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
		StringBuilder inputStringBuilder = new StringBuilder();
		try {
			new NonEmptyInputStream(response.getBody());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
			String line = bufferedReader.readLine();
			while (line != null) {
				inputStringBuilder.append(line);
				inputStringBuilder.append('\n');
				line = bufferedReader.readLine();
			}
		} catch (NonEmptyInputStream.EmptyInputStreamException | IOException e) {
			LOG.info("Empty body " + e.getMessage());
		}
		LOG.debug("============================ response for " + loggingRecord.getReqResPair() + " begin ==========================================");
		LOG.debug("Status code  : {}", response.getStatusCode());
		LOG.debug("Status text  : {}", response.getStatusText());
		LOG.debug("Headers      : {}", response.getHeaders());
		LOG.debug("Response body: {}", inputStringBuilder.toString());
		LOG.debug("======================= response for " + loggingRecord.getReqResPair() + " end =================================================");

		loggingRecord.setHttpStatus(response.getStatusCode().value());
		loggingRecord.setResponseHeaders(response.getHeaders().toString());
		if (inputStringBuilder.toString().contains("\u0000")) {
			loggingRecord.setResponseBody(inputStringBuilder.toString().split("\u0000")[0] + "...");
		} else {
			loggingRecord.setResponseBody(inputStringBuilder.toString());
		}

		loggingRecordRepository.save(loggingRecord);
	}
}
