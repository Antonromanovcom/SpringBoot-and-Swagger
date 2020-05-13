package online.prostobank.clients.api.dto.anketa;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class ApplicationAcceptedDTO {
	private boolean ok = true;
	private String loginUrl;
	private String message;
	private String requisites;
	private Object developerPayload;


	public static ApplicationAcceptedDTO ok(ApplicationAcceptedDTO dto) {
		return dto;
	}

	public static ApplicationAcceptedDTO error(ApplicationAcceptedDTO dto) {
		dto.setOk(false);
		return dto;
	}

	public ResponseEntity<ApplicationAcceptedDTO> response(HttpStatus status) {
		return new ResponseEntity<>(this, status);
	}
}
