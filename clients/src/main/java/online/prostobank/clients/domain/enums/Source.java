package online.prostobank.clients.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Source {
	API_TM("Холодная заявка"),     // холодная заявка
	API_ANKETA("Из анкеты V1"), // из анкеты
	API_ANKETA_V2("Из анкеты V2"), // из анкеты V2
	API_ANKETA_V3("Из анкеты V3"), // из анкеты V3
	API_ANKETA_V4("Из анкеты V4"), // из анкеты V4
	API_OUTER_CALL_CENTER("Холодная заявка из внешнего колл-центра"),    // из внешнего колл-центра
	API_TM_HOME("Холодная заявка домашнего колцентра"),
	API_TM_PARTNER("Холодная заявка партнерской сети"),
	API_TM_HUNTER("Полевая продажа"),
	CHATBOT("Чатбот"),
	API_DCC("Заявка ДКЦ")
	;

	private final String value;
}
