package online.prostobank.clients.domain.business_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "client_service_available")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientServiceAvailableEntity implements BusinessService {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private BusinessServiceEntity businessService;

    private long clientId;

    private boolean available;


    public ClientServiceAvailableEntity(BusinessServiceEntity businessService, long clientId, boolean available) {
        this.businessService = businessService;
        this.clientId = clientId;
        this.available = available;
    }

    @Override
    public boolean switchOn(boolean sw) {
        this.available = sw;
        return this.available;
    }

}
