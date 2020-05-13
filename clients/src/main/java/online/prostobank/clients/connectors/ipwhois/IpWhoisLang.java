package online.prostobank.clients.connectors.ipwhois;

import lombok.Getter;

@Getter
public enum IpWhoisLang {
    RU("ru"),
    EN("en");

    private final String lang;
    IpWhoisLang(String lang) {
        this.lang = lang;
    }
}
