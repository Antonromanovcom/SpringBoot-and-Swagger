package online.prostobank.clients.domain;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

/**
 * Хранит записи обращений и ответов к/от внешним системам
 */
@Entity
@Table(name = "logging_record")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
		discriminatorType = DiscriminatorType.STRING,
		name = "outer_connector"
)
public class LoggingRecord {

	@Id
	@GeneratedValue
	private Long id;
	private Integer httpStatus;
	private String path;
	private String method;
	//	private String clientIp;
	@Type(type = "org.hibernate.type.TextType")
	private String requestHeaders;
	@Type(type = "org.hibernate.type.TextType")
	private String responseHeaders;
	@Type(type = "org.hibernate.type.TextType")
	private String arguments;
	@Type(type = "org.hibernate.type.TextType")
	private String exceptions;
	private String reqResPair;
	//	@Type(type = "org.hibernate.type.TextType")
//	private String exceptions;
	@Type(type = "org.hibernate.type.TextType")
	private String requestBody;
	@Type(type = "org.hibernate.type.TextType")
	private String responseBody;
	private Instant requestTime;
	private Instant responseTime;

	public Long getId() {
		return id;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setMethod(String method) {
		this.method = method;
	}

//	public String getClientIp() {
//		return clientIp;
//	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public String getReqResPair() {
		return reqResPair;
	}
//	public String getExceptions() {
//		return exceptions;
//	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setReqResPair(String reqResPair) {
		this.reqResPair = reqResPair;
	}

	public void setRequestHeaders(String requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public void setResponseHeaders(String responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public void setRequestTime(Instant requestTime) {
		this.requestTime = requestTime;
	}

	public void setResponseTime(Instant responseTime) {
		this.responseTime = responseTime;
	}

	public void setExceptions(String exceptions) {
		this.exceptions = exceptions;
	}
}
