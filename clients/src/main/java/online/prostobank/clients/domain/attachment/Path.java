package online.prostobank.clients.domain.attachment;

import java.util.Objects;

/**
 * Обертка над строкой, содержащей путь в дереве элементов репозитория. Обертка позволяет в параметрах методов не путать
 * путь с иными строковыми параметрами.
 */
public class Path {
	private String path;

	public Path(String path) {
		this.path = path;
	}

	public String value() {
		return path;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Path path1 = (Path) o;
		return path.equals(path1.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}
