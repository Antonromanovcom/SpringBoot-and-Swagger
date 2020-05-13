package online.prostobank.clients.domain;

import lombok.Getter;
import online.prostobank.clients.domain.enums.BankId;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import java.io.Serializable;

import static online.prostobank.clients.utils.AutowireHelper.autowire;

/**
 * @author yv
 */
@Embeddable
public class AccountValue implements Serializable {
    @Getter private String accountNumber;
    @Getter private String requestId;
    private BankId bankId;

    @Autowired
    transient private BankInformation info;

    AccountValue() {
    }

    public AccountValue(String accountNumber, String requestId, BankId bankId) {
        this.accountNumber = accountNumber;
        this.requestId = requestId;
        this.bankId = bankId;
    }

    public @Nullable String getBankInn() {
        if (this.bankId == null) {
            return null;
        }
        if (info == null) {
            autowire(this);
        }
        return info.getInn(bankId);
    }

    public @Nullable String getBankBik() {
        // TODO: load bank inn from references
        if (this.bankId == null) {
            return null;
        }
        if (info == null) {
            autowire(this);
        }
        return info.getBik(bankId);
    }
}
