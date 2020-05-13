package online.prostobank.clients.api.dto.anketa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.Utm;

@Data
@Accessors(chain = true)
public class UtmDTO {
	@JsonProperty(value = "utm_source")
	private String source;
	@JsonProperty(value = "utm_campaign")
	private String campaign;
	@JsonProperty(value = "utm_content")
	private String content;
	@JsonProperty(value = "utm_medium")
	private String medium;
	@JsonProperty(value = "utm_term")
	private String term;
	@JsonProperty(value = "url")
	private String url;

	public static UtmDTO createFrom(Utm utm) {
		return new UtmDTO()
				.setTerm(utm.getUtmTerm())
				.setContent(utm.getUtmContent())
				.setCampaign(utm.getUtmCampaign())
				.setMedium(utm.getUtmMedium())
				.setSource(utm.getUtmSource())
				.setUrl(utm.getUrl());
	}
}
