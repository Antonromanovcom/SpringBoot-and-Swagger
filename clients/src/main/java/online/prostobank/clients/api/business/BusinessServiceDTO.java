package online.prostobank.clients.api.business;

import lombok.Data;
import online.prostobank.clients.domain.business_service.BusinessServiceEntity;

@Data
public class BusinessServiceDTO {
    private Long id;
    private String name;

    public BusinessServiceDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public BusinessServiceDTO(String name) {
        this.name = name;
    }

    public BusinessServiceDTO() {
    }

    public static BusinessServiceDTO getDto(BusinessServiceEntity entity){
        return new BusinessServiceDTO(entity.getId(), entity.getName());
    }
}
