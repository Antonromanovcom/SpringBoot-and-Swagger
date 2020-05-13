package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.api.dto.dictionary.StatusDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class ClientInfoDTO {
	private String email;
	private String phone;
	private String head;
	private CityDTO city;
	private String inn;
	private String ogrn;
	private String kpp;
	private String okved;
	private String promoCode;
	private String shortName;
	private String primaryCodes;
	private String secondaryCodes;
	private String riskyCodes;
	private String blackListedCodes;
	private List<FounderDTO> founders;
	private String clientTariffPlan;
	private Long clientCallback;
	private String loginURL;
	private Instant createdAt;
	private String name;
	private StatusDTO status;
	private DuplicateDTO duplicate;

	public static ClientInfoDTO createFrom(AccountApplication application, DuplicateDTO duplicate) {
		ClientValue client = application.getClient();
		List<FounderDTO> founderDTOS = client.getFounders().stream()
				.map(FounderDTO::createFrom)
				.collect(Collectors.toList());
		return builder()
				.email(client.getEmail())
				.phone(client.getPhone())
				.head(client.getHead())
				.city(CityDTO.createFrom(application.getCity()))
				.inn(client.getInn())
				.ogrn(client.getOgrn())
				.kpp(client.getKpp())
				.okved(application.getRiskyCodesString())
				.promoCode(client.getPromoInn())
				.shortName(client.getShortName())
				.primaryCodes(client.getPrimaryCodes())
				.secondaryCodes(client.getSecondaryCodes())
				.riskyCodes(client.getRiskyCodes())
				.blackListedCodes(client.getBlackListedCodes())
				.founders(founderDTOS)
				.loginURL(application.getLoginURL())
				.createdAt(application.getDateCreated())
				.name(client.getName())
				.status(StatusDTO.createFrom(application.getClientState()))
				.duplicate(duplicate)
				.clientTariffPlan(application.getClientTariffPlan())
				.clientCallback(application.getClientCallback() != null ? application.getClientCallback().toEpochMilli() : 0)
				.build();
	}
}
