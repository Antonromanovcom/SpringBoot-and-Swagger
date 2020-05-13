package online.prostobank.clients.services.dbo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class LegEntityDto {
	private String id;
	private String type;
	private String firsEntrance;
	private String name;
	private String inn;
	private Map<String, String> kpp;
	private Map<String, String> phone;
	private String phoneWork;
	private String phoneHome;
}
