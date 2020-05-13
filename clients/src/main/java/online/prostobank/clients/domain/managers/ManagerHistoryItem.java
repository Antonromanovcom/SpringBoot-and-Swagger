package online.prostobank.clients.domain.managers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Entity
public class ManagerHistoryItem {
	@Id
	@GeneratedValue
	private Long id;
	@Type(type = "org.hibernate.type.TextType")
	private String text;
	private Instant createdAt;
	private String eventInitiator;
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	private Manager manager;

	public ManagerHistoryItem(Manager manager, String text) {
		this.manager = manager;
		this.text = text;
		this.createdAt = Instant.now();
		this.eventInitiator = SecurityContextHelper.getCurrentUsername();
	}
}
