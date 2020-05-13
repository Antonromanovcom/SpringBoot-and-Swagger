package online.prostobank.clients.services.dbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DboRole {
	DBO_DEMO_USER("dbo-demo-user", "при регистрации пользователя в кейклоке"),
	DBO_USER("dbo-user", "при завершении открытия счета"),
	;

	private String name;
	private String description;
}
