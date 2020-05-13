package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Phone;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class PhoneDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "value")
	@HistoryField(title = "Телефон")
	private String value;
	@JsonProperty(value = "is_main")
	private boolean isMain;

	public PhoneDTO(Phone phone) {
		this.id = phone.getId();
		this.value = phone.getValue();
		this.isMain = phone.isMain();
	}

	public Phone toBusiness() {
		return new Phone(id, value,isMain);
	}
}
