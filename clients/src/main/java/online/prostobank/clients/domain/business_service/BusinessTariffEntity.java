package online.prostobank.clients.domain.business_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.business.BusinessTariffDTO;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "business_tariff")
@Data
@Accessors(chain = true)
public class BusinessTariffEntity {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    private boolean availableDemo;

    private boolean availablePayed;

    private String description;

    private String parameters;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private BusinessServiceEntity businessService;

    @Enumerated(value = EnumType.STRING)
    private DurationTariff durationDemo;

    @Enumerated(value = EnumType.STRING)
    private DurationTariff durationPayed;

    private Boolean archive;


    @JsonIgnore
    public BusinessTariffDTO getDto() {
        return new BusinessTariffDTO(id,
                name,
                availableDemo,
                availablePayed,
                description,
                parameters,
                businessService.getId(),
                durationDemo,
                durationPayed, archive);
    }

    @JsonIgnore
    public Instant getEndDate(Instant now, DurationTariff tariff) {
        Instant result;
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        switch (tariff) {
            case YEAR:
                result = localDateTime.plus(1, ChronoUnit.YEARS).minus(1, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
                break;

            case MONTH:
                result = localDateTime.plus(1, ChronoUnit.MONTHS).minus(1, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
                break;

            case THREE_DAYS:
                result = now.plus(3, ChronoUnit.DAYS);
                break;

            default:
                result = now;
        }
        return result;
    }
}
