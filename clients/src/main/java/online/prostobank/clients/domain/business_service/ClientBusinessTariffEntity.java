package online.prostobank.clients.domain.business_service;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "client_business_tariff")
@Data
@Accessors(chain = true)
public class ClientBusinessTariffEntity implements BusinessTariff{

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private ClientServiceAvailableEntity clientServiceAvailable;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private BusinessTariffEntity businessTariff;

    private boolean isDemo;

    private boolean isPayed;

    private Instant dateBegin;

    private Instant dateEnd;

    private Instant dateClose;

    private Instant firstEntrance;


    public ClientBusinessTariffEntity() {
    }

    public ClientBusinessTariffEntity(ClientServiceAvailableEntity clientServiceAvailable,
                                      @NotNull BusinessTariffEntity businessTariff,
                                      boolean isDemo,
                                      boolean isPayed,
                                      Instant dateBegin,
                                      Instant dateEnd,
                                      Instant dateClose) {
        this.clientServiceAvailable = clientServiceAvailable;
        this.businessTariff = businessTariff;
        this.isDemo = isDemo;
        this.isPayed = isPayed;
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.dateClose = dateClose;
    }

    public ClientBusinessTariffEntity(ClientServiceAvailableEntity clientServiceAvailable,
                                      @NotNull BusinessTariffEntity businessTariff,
                                      boolean isDemo,
                                      boolean isPayed) {
        this.clientServiceAvailable = clientServiceAvailable;
        this.businessTariff = businessTariff;
        this.isDemo = isDemo;
        this.isPayed = isPayed;
    }



    @Override
    public String description() {
        return this.businessTariff.getDescription();
    }

    @Override
    public String setDescription(String description) {
        this.businessTariff.setDescription(description);
        return this.businessTariff.getDescription();
    }

    @Override
    public boolean demo(boolean demo) {
        this.isDemo = demo;
        return this.isDemo;
    }

    @Override
    public boolean pay(boolean pay) {
        this.isDemo = false;
        this.isPayed = pay;
        return this.isPayed;
    }

    @Override
    public Long active() {
        return (dateClose==null) ? 0 : Duration.between(Instant.now(), dateClose).toHours();
    }

    @Override
    public boolean isActive() {
        return active() > 0;
    }

}
