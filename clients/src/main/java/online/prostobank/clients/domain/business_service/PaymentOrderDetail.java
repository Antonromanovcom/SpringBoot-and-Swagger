package online.prostobank.clients.domain.business_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.business.PaymentOrderDetailsRequest;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * APIKUB-2246
 *
 * Энтити и таблица и хранения реквизитов платежек по оплате сервисов/услуг
 */
@Entity
@Table(name = "payment_order_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderDetail {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private BusinessTariffEntity businessTariff; //тариф

    @Column(name = "inn_of_consumer")
    private String inn; // инн получателя

    @Column(name = "consumer_name")
    private String consumerName; //Наименование получателя

    @Column(name = "kpp_of_consumer")
    private String consumerKpp; //Кпп получателя

    @Column(name = "account_number")
    private String consumerAccountNumber; //Номер счёта получателя

    @Column(name = "bank_name")
    private String bankName; //Название банка получателя

    @Column(name = "bik")
    private String bik; //БИК банка получателя

    @Column(name = "corr_account")
    private String corrAccount; // Корреспондентский счёт банка получателя

    @Column(name = "amount")
    private BigDecimal amount; // Сумма платежа

    @Column(name = "description")
    private String description; // Назначение платежа

	public PaymentOrderDetail $update(PaymentOrderDetailsRequest request, BusinessTariffEntity businessTariffEntity) {
        this.inn = request.inn;
        this.consumerName  = request.name;
        this.consumerKpp = request.kpp;
        this.consumerAccountNumber = request.accountNumber;
        this.bankName = request.bankName;
        this.bik = request.bik;
        this.corrAccount = request.corrAccount;
        this.amount = request.amount;
        this.description = request.description;
        this.businessTariff = businessTariffEntity;
        return this;
	}
}
