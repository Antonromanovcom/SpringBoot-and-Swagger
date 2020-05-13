package online.prostobank.clients.services.business;

import online.prostobank.clients.api.business.BusinessTariffController;
import online.prostobank.clients.api.business.BusinessTariffDTO;
import online.prostobank.clients.api.business.PaymentOrderDetailsRequest;
import online.prostobank.clients.domain.business_service.BusinessTariffEntity;
import online.prostobank.clients.domain.business_service.PaymentOrderDetail;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;


public interface TariffService {

    /**
     * Получение списка тарифов по сервису
     * @param serviceId - id сервиса
     * @return Список тарифов
     */
    Optional<List<BusinessTariffEntity>> getListTariffByServiceId(@NotNull Long serviceId);

    /**
     * Сохранение/изменение тарифа
     * @param dto - dto тарифа
     * @return тариф
     */
    Optional<BusinessTariffDTO> saveTariff(@NotNull BusinessTariffDTO dto);

    //  ------------- БЛОК МЕТОДОВ ДЛЯ CRUD'А ПО РЕКВИЗИТАМ ДЛЯ ПЛАТЕЖЕК НА ОПЛАТУ ТАРИФОВ/УСЛУГ -----------------------

    /**
     * Платежные реквизиты по всем тарифам по сервису.
     *
     * @return
     */
    Optional<List<PaymentOrderDetail>> getAllPaymentOrderDetails(@NotNull Long serviceId);

    /**
     * Платежные реквизиты по конкретному тарифу.
     *
     * @return
     */
    Optional<PaymentOrderDetail> getPaymentOrderDetailsForOneTariff(@NotNull Long tariffId);


    /**
     * Добавить / редактировать платежные реквизиты по конкретному тарифу.
     *
     * @return
     */
    Optional<PaymentOrderDetail> addOrEditPaymentOrderDetails(PaymentOrderDetailsRequest payLoad);


    /**
     * Удалить имеющиеся платежные реквизиты.
     *
     * @return
     */
    Optional<List<PaymentOrderDetail>> deletePaymentOrderDetails(@NotNull Long detailId);
}
