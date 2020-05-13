package online.prostobank.clients.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class City {
	@Id
	@GeneratedValue
	private Long id;
	@NotNull
	private String name;
	private boolean isServiced;
}
