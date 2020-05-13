package online.prostobank.clients.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class InitiatePredictionResponseDTO {
	@JsonProperty(value = "status")
	private Integer status;
	@JsonProperty(value = "error")
	private String errorMessage;

	public boolean isValid() {
		return status != null && status == 0;
	}
}
