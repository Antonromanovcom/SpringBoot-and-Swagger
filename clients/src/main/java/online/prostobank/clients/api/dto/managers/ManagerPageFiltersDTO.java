package online.prostobank.clients.api.dto.managers;

import lombok.Data;

import java.util.List;

@Data
public class ManagerPageFiltersDTO {
	private List<String> roles;
	private List<Long> cities;
	private boolean online;
	private String search;
}
