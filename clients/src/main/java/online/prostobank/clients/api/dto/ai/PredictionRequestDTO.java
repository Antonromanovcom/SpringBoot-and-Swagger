package online.prostobank.clients.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PredictionRequestDTO {
	@JsonProperty(value = "id_list")
	private List<Long> ids;
}
