package online.prostobank.clients.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * Dto для транспорта данных о пользователе при логировании дийствий
 */
@Getter
@ToString
@AllArgsConstructor
public class UserInfoDto {
	private String email;
	private String preferredUsername;
	private UUID userId;
}
