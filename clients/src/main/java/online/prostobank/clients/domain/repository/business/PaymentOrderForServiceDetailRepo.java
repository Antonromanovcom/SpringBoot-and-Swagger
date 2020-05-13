package online.prostobank.clients.domain.repository.business;

import online.prostobank.clients.domain.business_service.PaymentOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderForServiceDetailRepo extends JpaRepository<PaymentOrderDetail, Long> {

    @Query(value = "select * from payment_order_details as pd, (select * from business_tariff as bsi" +
                   " where bsi.business_service_id=?1) as bt where pd.business_tariff_id = bt.id", nativeQuery = true)
    Optional<List<PaymentOrderDetail>> allPaymentOrderDetails(Long serviceId);

    Optional<PaymentOrderDetail> getPaymentOrderDetailByBusinessTariffId(Long serviceId);

    Optional<PaymentOrderDetail> getPaymentOrderDetailById(Long serviceId);

}
