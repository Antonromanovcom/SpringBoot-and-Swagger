package online.prostobank.clients.api.dto.dictionary;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.City;

import javax.annotation.Nullable;

@Data
@Accessors(chain = true)
public class CityDTO {
	private Long id;
	private String name;
	private boolean isServiced;
	@Nullable
	private String partner;

	public static CityDTO createFrom(City city) {
		return new CityDTO()
				.setId(city.getId())
				.setName(city.getName())
				.setServiced(city.isServiced());
	}

	public static CityDTO createFrom(City city, String partner) {
		return new CityDTO()
				.setId(city.getId())
				.setName(city.getName())
				.setServiced(city.isServiced())
				.setPartner(partner);
	}
}
