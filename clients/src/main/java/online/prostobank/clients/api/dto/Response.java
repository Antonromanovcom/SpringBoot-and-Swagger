package online.prostobank.clients.api.dto;

import online.prostobank.clients.utils.Utils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public class Response<T> implements Serializable {
	Error error;
	T result;

	public static <T> ResponseEntity<String> ok(T content) {
		Response<T> r = new Response<>();
		r.result = content;
		return r.toResponseEntity();
	}

	public static <T> ResponseEntity<String> exception(String message) {
		Response<T> r = new Response<>();
		r.error = new Error(message);
		return r.toResponseEntity();
	}


	public ResponseEntity<String> toResponseEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=UTF-8");
		return new ResponseEntity<String>(toJson(), headers, HttpStatus.OK);
	}

	private String toJson() {
		return Utils.toJson(this);
	}

	static class Error implements Serializable {
		public String message;
		public String faultInfo;

		public Error(String message, String faultInfo) {
			this.message = message;
			this.faultInfo = faultInfo;
		}

		public Error(String message) {
			this.message = message;
		}
	}
}
