package online.prostobank.clients.api.dto.anketa;

import lombok.Data;
import online.prostobank.clients.api.dto.PassportInfoDTO;

@Data
public class AccountApplicationDTO {
    private ContactInfoDTO contactInfo;
    private PassportInfoDTO passportInfo;
}
