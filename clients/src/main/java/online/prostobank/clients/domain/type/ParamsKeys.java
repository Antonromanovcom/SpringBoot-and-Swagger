package online.prostobank.clients.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParamsKeys {
    EVENT_ID("eventId"),
    EMAIL("to"),
    PHONE("phone"),
    CRM_ID("vnut_id_crm"),
    CITY("city"),
    FIRST_NAME("firstName"),
    ACCOUNT_NUMBER("accountNumber"),
    SECOND_NAME("secondName"),
    APPOINTMENT_DATE("appointmentDate"),
    APPOINTMENT_TIME("appointmentTime"),
    ACCOUNT_LINK("accountLink"),
    CLIENT_DENIED_COMMENT("clientDeniedComment"),
    COMPANY_NAME("companyName"),
    ORIGIN_SOURCE("originSource"),
    INN("inn"),
    ACCEPTANCE("acceptance"),
    KOD_ORG_NUMBER("kodOrgnomer"),
    KPP_OGRNIP("kppOgrnip"),
    CONFIRMATION_CODE("confirmationCode"),
    LOGIN("login"),
    PASSWORD("password"),
    DBO_LINK("dboLink"),
    HREF("href"),
    TAX_NUMBER("taxNumber"),
    DATE_TIME("dateTime"),
    OKVED_LIST("okvedList"),
    FEATURE_LIST("featureList"),
    SCORING_LIST("scoringList"),
    POINTS("points"),
    FULL_NAME("fullName"),
    UID("uid"),
    CREATE_STAMP("createStamp"),
    MESSAGE("message"),
    ;

    private final String key;
}
