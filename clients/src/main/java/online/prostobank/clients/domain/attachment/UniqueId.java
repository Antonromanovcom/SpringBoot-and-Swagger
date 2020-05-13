package online.prostobank.clients.domain.attachment;

import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.UUID;

public class UniqueId implements IUniqueId {
	private String id;

	public UniqueId() {
		this.id = UUID.randomUUID().toString();
	}

	public UniqueId(@NonNull String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UniqueId uniqueId = (UniqueId) o;
		return id.equals(uniqueId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return id;
	}
}
