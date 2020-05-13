package online.prostobank.clients.domain.managers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.dto.managers.ManagerDTO;
import online.prostobank.clients.domain.City;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.keycloak.representations.idm.UserRepresentation;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(name = "manager")
public class Manager {
	@Id
	@NotNull
	private UUID id;
	@NotNull
	private String login;

	private String firstName;
	private String secondName;
	private String lastName;

	@ManyToOne(fetch = FetchType.EAGER)
	private City city;
	private String phone;

	@OneToMany(orphanRemoval = true, mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@BatchSize(size = 5)
	private Set<ManagerHistoryItem> managerHistoryItems = new HashSet<>();
	private Instant lastOnline;

	@Nonnull
	public static Manager createFrom(@Nonnull UserRepresentation user, City city) {
		return new Manager()
				.setId(UUID.fromString(user.getId()))
				.setLogin(user.getUsername())
				.setFirstName(user.getFirstName())
				.setLastName(user.getLastName())
				.setCity(city);
	}

	public String compareFields(ManagerDTO managerDto) {
		StringBuilder sb = new StringBuilder();

		Arrays.stream(ManagerField.values())
				.forEach(managerField -> {
					String oldValue = managerField.getManagerGetter().apply(this);
					String newValue = managerField.getManagerDtoGetter().apply(managerDto);

					if (!Objects.equals(oldValue, newValue)) {
						sb.append(managerField.getKey())
								.append(": было '")
								.append(oldValue)
								.append("' стало '")
								.append(newValue)
								.append("'.\r\n");
					}
				});

		return sb.toString();
	}
}
