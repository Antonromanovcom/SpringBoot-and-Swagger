package online.prostobank.clients.domain.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Дополнительные мета-данные документа, сериализуемые и сохраняемые в форме json
 */
@Getter
@Setter
public class DocumentAux {
	@JsonProperty(value = "quality")
	private Boolean quality; //объект т.к. на самом деле три состояния хороший/плохой/нет оценки
	@JsonProperty(value = "verified")
	private boolean verified;
	@JsonProperty(value = "error_message")
	private String errorMessage;
	@JsonProperty(value = "is_damaged")
	private boolean isDamaged;
}
