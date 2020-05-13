package online.prostobank.clients.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.dto.anketa.UtmDTO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Полученные от анкеты utm метки
 */
@Data
@Accessors(chain = true)
@Entity
public class Utm {
	@Id
	@GeneratedValue
	private Long id;
	private String utmSource;
	private String utmCampaign;
	private String utmContent;
	private String utmMedium;
	private String utmTerm;
	private String url;

	public static Utm createFrom(UtmDTO dto) {
		return new Utm()
				.setUtmCampaign(dto.getCampaign())
				.setUtmContent(dto.getContent())
				.setUtmMedium(dto.getMedium())
				.setUtmSource(dto.getSource())
				.setUtmTerm(dto.getTerm())
				.setUrl(dto.getUrl());
	}
}
