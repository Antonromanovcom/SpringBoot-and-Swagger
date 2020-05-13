package online.prostobank.clients.domain.business_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.business.BusinessServiceDTO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "business_service")
@Data
@Accessors(chain = true)
public class BusinessServiceEntity {

    @Id
    @GeneratedValue
    private long id;

    private String name;


    @JsonIgnore
    public BusinessServiceDTO getDto() {
        return new BusinessServiceDTO(id, name);
    }

}
