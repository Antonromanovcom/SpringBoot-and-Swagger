package online.prostobank.clients.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StartWork {
	@Setter
	@Id
	@GeneratedValue
	private Long id;
	private Instant startAt;
	private String manager;
	@Setter
	private String status;
	@Setter
	@Getter(onMethod = @__(@JsonIgnore))
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	private AccountApplication app;

	public StartWork(AccountApplication app, String manager, String status) {
		this.app = app;
		this.manager = manager;
		this.startAt = Instant.now();
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StartWork startWork = (StartWork) o;
		return id.equals(startWork.id) &&
				startAt.equals(startWork.startAt) &&
				manager.equals(startWork.manager) &&
				app.equals(startWork.app);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, startAt, manager, app.getId());
	}
}
