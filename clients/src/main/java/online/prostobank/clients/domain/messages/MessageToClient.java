package online.prostobank.clients.domain.messages;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class MessageToClient {
	@Id
	@GeneratedValue
	private Long id;
	private Long clientId;
	private UUID manager;
	private String managerLogin;
	@Type(type = "org.hibernate.type.TextType")
	private String text;
	@CreationTimestamp
	private Instant createdAt;

	public MessageToClient(Long clientId, UUID manager, String managerLogin, String text) {
		this.clientId = clientId;
		this.text = text;
		this.manager = manager;
		this.managerLogin = managerLogin;
	}
}
