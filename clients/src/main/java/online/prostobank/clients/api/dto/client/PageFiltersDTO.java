package online.prostobank.clients.api.dto.client;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PageFiltersDTO {
	private String search;
	private boolean onlySelf;
	private List<Integer> statuses;
	private List<Long> cities;
	private Instant updatedAt; //сутки, в диапазоне которых производится фильтрация по времени изменения
	private Instant createdAt; //сутки, в диапазоне которых производится фильтрация по времени создания
}
