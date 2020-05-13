package online.prostobank.clients.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DccResponseDTO {
	OK(0, "Заявка успешно создана."),
	WRONG_INN(1, "Неверный ИНН/ОГРН. Обязательное поле."),
	ALREADY_EXIST(2, "Пользователь с ИНН/ОГРН уже зарегистрирован."),
	NO_PHONE_PROVIDED(3, "Неверный номер телефона. Обязательное поле."),
	WRONG_CITY(4, "Неверный город."),
	NO_OPERATOR_PROVIDED(5, "Отсутствует оператор. Обязательное поле.");

	@JsonProperty("code")
	private Integer code;

	@JsonProperty("message")
	private String message;

	DccResponseDTO(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}