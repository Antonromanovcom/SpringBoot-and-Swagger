package online.prostobank.clients.domain.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ClientKeycloak {
	@Id
	private Long clientId;
	private String login;
	private UUID keycloakId;
	private Instant createdAt;
}
