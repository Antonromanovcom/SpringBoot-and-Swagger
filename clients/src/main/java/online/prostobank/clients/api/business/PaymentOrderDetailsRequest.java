package online.prostobank.clients.api.business;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * ДТО-ха для сохранения новых реквизитов платежа.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderDetailsRequest {
    @NotNull
    public Long tariff;
    public String inn;
    public String name;
    public String kpp;
    public String accountNumber;
    public String bankName;
    public String bik;
    public String corrAccount;
    public BigDecimal amount;
    public String description;
}
