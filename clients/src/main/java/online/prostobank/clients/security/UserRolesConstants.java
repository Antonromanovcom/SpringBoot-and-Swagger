package online.prostobank.clients.security;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class UserRolesConstants {
	// Группа Простобанка
	public static final String ROLE_POS_ADMIN = "pos-admin";
	public static final String ROLE_POS_FRONT = "pos-front";
	// Группа Домашнего Колл Центра
	public static final String ROLE_POS_ADMIN_HOME = "pos-admin-home";
	public static final String ROLE_POS_FRONT_HOME = "pos-front-home";
	// Группа Партнерских сетей
	public static final String ROLE_POS_ADMIN_PARTNER = "pos-admin-partner";
	public static final String ROLE_POS_FRONT_PARTNER = "pos-front-partner";
	// Группа Внешних Партнеров
	public static final String ROLE_POS_OUTER_API_ADMIN = "pos-outer_api_admin";
	public static final String ROLE_POS_OUTER_API_MANAGER = "pos-outer_api_manager";
	// Группа
	public static final String ROLE_POS_CONSULTANT = "pos-consultant";

	public static final String ROLE_SUPER_USER = "super-user";

	public static final Set<String> ROLES_ALL = ImmutableSet.of(
			ROLE_POS_ADMIN,
			ROLE_POS_FRONT,
			ROLE_POS_ADMIN_HOME,
			ROLE_POS_FRONT_HOME,
			ROLE_POS_ADMIN_PARTNER,
			ROLE_POS_FRONT_PARTNER,
			ROLE_POS_OUTER_API_ADMIN,
			ROLE_POS_OUTER_API_MANAGER,
			ROLE_POS_CONSULTANT,
			ROLE_SUPER_USER
	);

	public static final Set<String> ROLES_MANAGERS = ImmutableSet.of(
			ROLE_POS_ADMIN,
			ROLE_POS_FRONT,
			ROLE_POS_ADMIN_HOME,
			ROLE_POS_FRONT_HOME,
			ROLE_POS_ADMIN_PARTNER,
			ROLE_POS_FRONT_PARTNER,
			ROLE_POS_OUTER_API_ADMIN,
			ROLE_POS_OUTER_API_MANAGER,
			ROLE_POS_CONSULTANT
	);

	public static final Set<String> ROLES_MANAGERS_HOME = ImmutableSet.of(
			ROLE_POS_ADMIN_HOME,
			ROLE_POS_FRONT_HOME
	);
	public static final Set<String> ROLES_MANAGERS_PARTNER = ImmutableSet.of(
			ROLE_POS_ADMIN_PARTNER,
			ROLE_POS_FRONT_PARTNER
	);
	public static final Set<String> ROLES_MANAGERS_OUTER = ImmutableSet.of(
			ROLE_POS_OUTER_API_ADMIN,
			ROLE_POS_OUTER_API_MANAGER
	);

	public static final Set<String> NO_ACCOUNT_NUMBER = ImmutableSet.of(
			ROLE_POS_ADMIN_HOME,
			ROLE_POS_FRONT_HOME,
			ROLE_POS_ADMIN_PARTNER,
			ROLE_POS_FRONT_PARTNER
	);
}
