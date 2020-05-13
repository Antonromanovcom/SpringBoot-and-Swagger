package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.domain.enums.BankId;

@Getter
@AllArgsConstructor
public class AccountValueDTO {
    private String accountNumber;
    private String requestId;
    private BankId bankId;
}
