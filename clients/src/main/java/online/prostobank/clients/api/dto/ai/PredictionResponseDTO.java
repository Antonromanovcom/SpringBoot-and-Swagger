package online.prostobank.clients.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class PredictionResponseDTO {
	@JsonProperty(value = "status")
	private Integer status;
	@JsonProperty(value = "id_list")
	private List<Long> ids;
	@JsonProperty(value = "scores")
	private List<Double> scores;
	@JsonProperty(value = "error")
	private String errorMessage;

	@JsonIgnore
	public boolean isValid() {
		return status != null && status == 0;
	}

	public List<Pair<Long, Double>> toBusiness() {
		if (isValid() && ids != null && scores != null && ids.size() == scores.size()) {
			List<Pair<Long, Double>>  result = new ArrayList<>();
			for (int i = 0; i < ids.size(); i++) {
				result.add(Pair.of(ids.get(i), scores.get(i)));
			}
			return result;
		}
		return Collections.emptyList();
	}
}
