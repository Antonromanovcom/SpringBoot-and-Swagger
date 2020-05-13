package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.Founder;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ClientValueDTO {
    private String                   name;
    private String                   shortName;
    private String                   email;
    private String                   phone;
    private String                   inn;
    private String                   promoInn;
    private String                   ogrn;
    private String                   head;
    private String                   address;
    private String                   kpp;
    private String                   residentAddress;
    private String                   primaryCodes;
    private String                   secondaryCodes;
    private String                   blackListedCodes;
    private String                   riskyCodes;
    private CompanyKonturFeatureDTO  konturFeature;
    private CompanyKycScoringDTO     companyKycScoring;
    private String                   headTaxNumber;
    private LocalDate                regDate;
    private LocalDate                grnRecordDate;
    private Set<Founder>             founders;

    public ClientValueDTO(String name, String head, String ogrn, String inn) {
        this.name = name;
        this.head = head;
        this.ogrn = ogrn;
        this.inn = inn;
    }

    public @Nonnull ClientValue toModel() {
        return new ClientValue(
                this.name,
                this.email,
                this.phone,
                this.inn,
                this.ogrn,
                this.head
        );
    }
}
