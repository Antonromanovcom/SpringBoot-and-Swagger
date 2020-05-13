package online.prostobank.clients.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static online.prostobank.clients.domain.type.ParamsKeys.PHONE;

public class Email implements Serializable {

    /**
     * Виды писем
     */
    @Getter
    @RequiredArgsConstructor
    public enum EmailId {
        PASSPORT("passport-denied", "DENIED-PASSPORT"),
        FNS("fns-denied", "DENIED-FNS"),
        BANKRUPCY("bankrupcy-denied", "DENIED-BANKRUPCY"),
        OKVEDS("okved-denied", "DENIED-OKVED"), // 2398
        ERR_SECURITY_DECLINE("security-denied", "ERR_SECURITY_DECLINE"), // 2397
        KYC("security-denied", "DENIED-KYC"), // 2397
        SCORING("security-denied", "DENIED-SCORING"), // 2397
        P550("security-denied", "DENIED-550"), // 2397
        BANK_REFUSED("security-denied", "ERR_BANK_DECLINE"), // 2397
        POS_BACK_REFUSED("pos-back-refused", "DENIED-BACK"), // 2660
        ARREST_DECLINE("arrest-decline", "DENIED_ARREST"), // 4608
        ERR_CLIENT_DECLINE("", "ERR_CLIENT_DECLINE"),
        ERR_MANAGER_DECLINE("", "ERR_MANAGER_DECLINE"),
        RESERVING("reserving", "RESERVING"),
        ACCOUNT_RESERVED("account-reserved", "NEW"),
        ACCOUNT_RESERVED_FOR_TRANSPORTATION("account-reserved", "NEW-T"),
        ACCOUNT_RESERVED_FOR_REALTORS("account-reserved", "NEW-R"),
        FULFILLED_MANUAL_NOTIFY("account-opened", "FULFILLED"),
        FULFILLED_AUTO_NOTIFY("account-opened", "FULFILLED"),
        APPOINTMENT_MADE_MANUAL_NOTIFY("appointment", "APPOINTMENT_MADE"),
        APPOINTMENT_MADE_AUTO_NOTIFY("appointment", "APPOINTMENT_MADE"),
        NO_ANSWER("", "NO_ANSWER"),
        WAIT_FOR_DOCS("", "WAIT_FOR_DOCS"),
        NOT_COME("", "NOT-COME"),
        WELCOME("", "CONTACT_INFO_CONFIRMED"),
        DUBLICATE("", ""),
        ATTACHMENT_DOCUMENTS_NEW("", "NEW"),
        ATTACHMENT_DOCUMENTS_NEW_T("", "NEW-T"),
        NOW_SIGNING("", "NOW_SIGNING"),
        CLOSED("", "CLOSED"),
        DELETE("", "DELETE"),
        ;

        public final String theme;
        public final String emarsysStatusName;
    }

    public final Email.EmailId id;
    public final String email;
    public final HashMap<String, Object> obj;
    public final Long applicationId;

    public Email(Email.EmailId id, Long applicationId, String email, Map<String, Object> obj) {
        this.id = id;
        this.email = email;
        this.obj = new HashMap<>(obj);
        this.applicationId = applicationId;
        for (String s : this.obj.keySet()) {
            if (s.equals(PHONE.getKey())) {
                if (this.obj.get(s) instanceof String) {
                    this.obj.put(s, "7" + this.obj.get(s)); // fix phone numbers for Emarsys
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("EmailPayload{addr='%s', text='%s', template='%s'}", email, mapToStr(obj), id == null ? "-" : id.getTheme());
    }

    private static String mapToStr(Map<String, Object> map) {
        return map.toString();
    }
}
