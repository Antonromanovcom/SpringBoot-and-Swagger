package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Data;
import online.prostobank.clients.domain.Founder;

import java.time.LocalDate;

@Data
@Builder
public class FounderDTO {
	private Long id;
	private String fio;
	private String inn;
	private String ogrn;
	private LocalDate grnRecordDate;

	public static FounderDTO createFrom(Founder founder) {
		return builder()
				.id(founder.getId())
				.fio(founder.getFio())
				.inn(founder.getInn())
				.ogrn(founder.getOgrn())
				.grnRecordDate(founder.getGrnRecordDate())
				.build();
	}
}
