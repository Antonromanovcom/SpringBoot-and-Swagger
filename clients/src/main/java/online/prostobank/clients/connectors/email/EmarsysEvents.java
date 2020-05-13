package online.prostobank.clients.connectors.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmarsysEvents {
    ACCOUNT_RESERVED("338"),
    ACCOUNT_RESERVED_FOR_TRANSPORTATION("396"),
    FULFILLED_MANUAL_NOTIFY("397"),        //уведомление отправляется менеджером
    FULFILLED_AUTO_NOTIFY("2401"),         //автоматическое при смене статуса
    APPOINTMENT_MADE_MANUAL_NOTIFY("398"), //уведомление отправляется менеджером
    APPOINTMENT_MADE_AUTO_NOTIFY("2400"),  //автоматическое при смене статуса
    TO_NEW_OR_NEW_T_WITH_LOADING("2292"),  //уведомление с функционалом загрузки документов
    TO_NEW_OR_NEW_T_ONLY_NOTIFY("2399"),   //уведомление без функционала загрузки
    DENIED_OKVED("2398"),                  //уведомление об отказе по причине несоотв. ОКВЭД
    DENIED_OTHER_CONDITION("2397"),        //уведомление об отказе по иным причинам
    DENIED_ARREST("4608"),                 //уведобление об отказе по причине наличия арестов
    ATTACHMENT_DOCUMENTS_NEW("2410"),
    DECLINED_AFTER_APPOINTMENT_MADE("2522"),
    ACCOUNT_RESERVED_FOR_REALTORS("2593"), //есть оквэд по недвижимости
    DENIED_BACK("2660"),                   //уведомление об отказе бэк-менеджером
    MESSAGE_TO_CLIENT_EVENT("3154"),       //отправка сообщения клиенту от менеджера
    ;

    private final String emarsysEventNumber;
}
