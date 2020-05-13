package online.prostobank.clients.services;

import com.google.common.collect.ImmutableMap;
import online.prostobank.clients.api.dto.client.PageInfoDTO;
import online.prostobank.clients.api.dto.client.PageSortDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GridUtils {
	public static final Map<String, String> CLIENT_ALIAS_MAP = ImmutableMap.<String, String>builder()
			.put("clientId", "id")
			.put("createdAt", "dateCreated")
			.put("city", "city.name")
			.put("client", "client.name")
			.put("inn", "client.inn")
			.put("phone", "client.phone")
			.put("status", "status.status")
			.put("updateDateTime", "updateDateTime")
			.put("aiScore", "aiScore")
			.build();

	public static final Map<String, String> MANAGER_ALIAS_MAP = ImmutableMap.<String, String>builder()
			.put("id", "id")
			.put("name", "login")
			.build();

	public static PageRequest getPaging(PageInfoDTO pageDTO, Sort sorting) {
		return PageRequest.of(pageDTO.getCurrent(), pageDTO.getSize(), sorting);
	}

	public static Sort getSorting(PageSortDTO sortDTO, Map<String, String> aliasMap) {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		if (sortDTO != null) {
			//совместимость с предыдущим вариантом, когда поле сортировки предусматривалось одно
			if (sortDTO.getSortFields() == null || sortDTO.getSortFields().isEmpty()) {
				if (aliasMap.containsKey(sortDTO.getBy())) {
					Sort.Direction direction = sortDTO.isReverse() ? Sort.Direction.DESC : Sort.Direction.ASC;
					sort = Sort.by(direction, aliasMap.get(sortDTO.getBy()));
				}
			} else {
				List<Sort.Order> orders = new ArrayList<>();
				for (PageSortDTO field : sortDTO.getSortFields()) {
					if (aliasMap.containsKey(field.getBy())) {
						Sort.Direction direction = field.isReverse() ? Sort.Direction.DESC : Sort.Direction.ASC;
						orders.add(new Sort.Order(direction, aliasMap.get(field.getBy())));
					}
				}
				sort = Sort.by(orders);
			}
		}
		return sort;
	}

	public static String getSearchString(String search) {
		if (search != null) {
			String searchString = StringUtils.trim(search);
			searchString = searchString.equals(StringUtils.EMPTY) ? null : "%" + searchString + "%";
			return searchString;
		}
		return null;
	}
}
