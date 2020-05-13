package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Email;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class EmailDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "value")
	@HistoryField(title = "Адрес email")
	private String value;
	@JsonProperty(value = "is_main")
	private boolean isMain;

	public EmailDTO(Email email) {
		this.id = email.getId();
		this.value = email.getValue();
		this.isMain = email.isMain();
	}

	public Email toBusiness() {
		return new Email(id, value, isMain);
	}
}
