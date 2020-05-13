package online.prostobank.clients.api.business;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.business_service.DurationTariff;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessTariffDTO {

    private Long id;

    private String name;

    private boolean availableDemo;

    private boolean availablePayed;

    private String description;

    private String parameters;

    private Long businessServiceId;

    private DurationTariff durationDemo;

    private DurationTariff durationPayed;

    private boolean archive;
}
