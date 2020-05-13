package online.prostobank.clients.services;

import online.prostobank.clients.domain.LoggingRecord;
import online.prostobank.clients.domain.repository.LoggingRecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class LoggingService {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingService.class);
	public static final String REQ_RES_PAIR_ATTRIBUTE = "anlore";

	@Autowired
	private LoggingRecordRepository loggingRecordRepository;

	public void logRequest(HttpServletRequest request, Object requestBody) {
		/*LoggingRecord loggingRecord = new AnketaEndpointLoggingRecord();
		loggingRecord.setRequestTime(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
		String uuid = UUID.randomUUID().toString();
		loggingRecord.setReqResPair(uuid);

		request.setAttribute(REQ_RES_PAIR_ATTRIBUTE, uuid);
		Map<String, String> headersMap = buildHeadersMap(request);

		String requestURI = request.getRequestURI();
		String method = request.getMethod();
		String body = "";
		if (requestBody != null) {
			body = JsonStream.serialize(requestBody);
		}
		Map<String, String> arguments = buildParametersMap(request);
		String argumentsString = StringUtils.join(arguments);

		LOG.debug("=========================== request for " + loggingRecord.getReqResPair() + " begin ================================================");
		LOG.debug("URI          : {}", requestURI);
		LOG.debug("Method       : {}", method);
		LOG.debug("Headers      : {}", headersMap);
		LOG.debug("Request body : {}", body);
		LOG.debug("Arguments    : {}", argumentsString);
		LOG.debug("========================== request for " + loggingRecord.getReqResPair() + " end ================================================");

		loggingRecord.setPath(requestURI);
		loggingRecord.setMethod(method);
		loggingRecord.setRequestHeaders(String.valueOf(headersMap));
		loggingRecord.setRequestBody(body);
		loggingRecord.setArguments(argumentsString);
		loggingRecordRepository.save(loggingRecord);*/
	}

	public void logResponse(HttpServletRequest request, HttpServletResponse response, Object body) {
		/*LoggingRecord loggingRecord = getLoggingRecord(request);
		if (loggingRecord == null) {
			LOG.error("Unable to obtain logging record for session id {} and body {}", request.getSession().getId(), body);
			loggingRecord = new AnketaEndpointLoggingRecord();
		}
		String responseBodyString;
		if (body instanceof String) {
			responseBodyString = (String) body;
		} else if (body != null) {
			responseBodyString = JsonStream.serialize(body);
		} else {
			LOG.warn("Trying to log empty response body for URI " + request.getRequestURI());
			responseBodyString = "";
		}
		loggingRecord.setResponseTime(Instant.now().atZone(ZoneId.systemDefault()).toInstant());

		String headersString = StringUtils.join(buildHeadersMap(response));

		LOG.debug("============================ response for " + loggingRecord.getReqResPair() + " begin ==========================================");
		int responseStatus = response.getStatus();
		LOG.debug("Status code  : {}", responseStatus);
		LOG.debug("Headers      : {}", headersString);
		LOG.debug("Response body: {}", responseBodyString);
		LOG.debug("======================= response for " + loggingRecord.getReqResPair() + " end =================================================");

		loggingRecord.setHttpStatus(responseStatus);
		loggingRecord.setResponseHeaders(headersString);
		loggingRecord.setResponseBody(responseBodyString);

		if (StringUtils.isBlank(loggingRecord.getPath())) { // бывает не записывается если 500
			loggingRecord.setPath(request.getRequestURI());
		}

		loggingRecordRepository.save(loggingRecord);*/
	}

	public void logResponseWithoutBody(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		/*LoggingRecord loggingRecord = getLoggingRecord(request);
		if (loggingRecord == null) {
			loggingRecord = new AnketaEndpointLoggingRecord();
		}

		if (StringUtils.isBlank(loggingRecord.getResponseBody())) {
			loggingRecord.setResponseTime(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
			loggingRecord.setHttpStatus(response.getStatus());
			loggingRecord.setResponseHeaders(StringUtils.join(buildHeadersMap(response)));
		}

		if (ex != null) {
			loggingRecord.setExceptions(ex.getLocalizedMessage());
		}

		if (StringUtils.isBlank(loggingRecord.getPath())) { // бывает не записывается если 500
			loggingRecord.setPath(request.getRequestURI());
		}

		loggingRecordRepository.save(loggingRecord);*/
	}

	private LoggingRecord getLoggingRecord(HttpServletRequest request) {
		String attribute = (String) request.getAttribute(REQ_RES_PAIR_ATTRIBUTE);
		if (StringUtils.isBlank(attribute)) {
			attribute = UUID.randomUUID().toString();
		}
		return loggingRecordRepository.getAllByReqResPair(attribute);
	}

	private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
		Map<String, String> resultMap = new HashMap<>();
		Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			String value = httpServletRequest.getParameter(key);
			resultMap.put(key, value);
		}

		return resultMap;
	}

	private Map<String, String> buildHeadersMap(HttpServletRequest request) {
		Map<String, String> map = new HashMap<>();

		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}

		return map;
	}

	private Map<String, String> buildHeadersMap(HttpServletResponse response) {
		Map<String, String> map = new HashMap<>();

		Collection<String> headerNames = response.getHeaderNames();
		for (String header : headerNames) {
			map.put(header, response.getHeader(header));
		}

		return map;
	}
}
