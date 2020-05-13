package online.prostobank.clients.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

import static online.prostobank.clients.security.UserRolesConstants.*;

@Getter
@AllArgsConstructor
public enum UserRoles {
	POS_ADMIN(ROLE_POS_ADMIN, "Администратор"),
	POS_FRONT(ROLE_POS_FRONT, "Фронт-менеджер"),
	POS_ADMIN_HOME(ROLE_POS_ADMIN_HOME, "Админ «Домашнего коллцентра»"),
	POS_FRONT_HOME(ROLE_POS_FRONT_HOME, "Фронт «Домашнего коллцентра»"),
	POS_ADMIN_PARTNER(ROLE_POS_ADMIN_PARTNER, "Админ «Партнерской сети»"),
	POS_FRONT_PARTNER(ROLE_POS_FRONT_PARTNER, "Фронт «Партнерской сети»"),
	POS_OUTER_API_ADMIN(ROLE_POS_OUTER_API_ADMIN, "Админ по работе с заявками внешнего колл-центра"),
	POS_OUTER_API_MANAGER(ROLE_POS_OUTER_API_MANAGER, "Менеджер по работе с заявками внешнего колл-центра"),
	POS_CONSULTANT(ROLE_POS_CONSULTANT, "Консультант"),
	POS_SUPER_USER(ROLE_SUPER_USER, "Супер-юзер"),
	;

	private final String roleName;
	private final String humanReadable;

	/**
	 * Возвращает человекочитаемое название роли для отображения в CRM
	 */
	public static String getHumanReadableByRoleName(@NonNull String roleName) {
		return Arrays.stream(values())
				.filter(role -> roleName.equals(role.getRoleName()))
				.findFirst()
				.map(UserRoles::getHumanReadable)
				.orElse("");
	}
}
