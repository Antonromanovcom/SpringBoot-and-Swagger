package online.prostobank.clients.services.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.business.BusinessTariffController;
import online.prostobank.clients.api.business.BusinessTariffDTO;
import online.prostobank.clients.api.business.PaymentOrderDetailsRequest;
import online.prostobank.clients.domain.business_service.BusinessServiceEntity;
import online.prostobank.clients.domain.business_service.BusinessTariffEntity;
import online.prostobank.clients.domain.business_service.PaymentOrderDetail;
import online.prostobank.clients.domain.repository.business.BusinessServiceRepository;
import online.prostobank.clients.domain.repository.business.BusinessTariffRepository;
import online.prostobank.clients.domain.repository.business.PaymentOrderForServiceDetailRepo;
import online.prostobank.clients.services.business.TariffService;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessTariffService implements TariffService {
    private final BusinessTariffRepository tariffRepository;
    private final BusinessServiceRepository serviceRepository;
    private final PaymentOrderForServiceDetailRepo paymentDetailsRepo;

    @Override
    public Optional<List<BusinessTariffEntity>> getListTariffByServiceId(@NotNull Long serviceId) {
        return tariffRepository.allTariffByService(serviceId);
    }

    @Override
    public Optional<BusinessTariffDTO> saveTariff(@NotNull BusinessTariffDTO dto) {
        Optional<BusinessServiceEntity> serviceEntity = serviceRepository.findById(dto.getBusinessServiceId());
        if (serviceEntity.isPresent()) {
            Long tariffId = Optional.ofNullable(dto.getId()).orElse(-1L);
            Optional<BusinessTariffEntity> tariffEntity = tariffRepository.findById(tariffId);
                BusinessTariffEntity businessTariffEntity = tariffEntity.map(tariff -> tariff.setName(dto.getName())
                        .setDescription(dto.getDescription())
                        .setAvailableDemo(dto.isAvailableDemo())
                        .setAvailablePayed(dto.isAvailablePayed())
                        .setBusinessService(serviceEntity.get())
                        .setParameters(dto.getParameters())
                        .setDurationDemo(dto.getDurationDemo())
                        .setDurationPayed(dto.getDurationPayed())
                        .setArchive(dto.isArchive()))
                        .orElseGet(() -> new BusinessTariffEntity()
                                .setName(dto.getName())
                                .setDescription(dto.getDescription())
                                .setAvailableDemo(dto.isAvailableDemo())
                                .setAvailablePayed(dto.isAvailablePayed())
                                .setBusinessService(serviceEntity.get())
                                .setParameters(dto.getParameters())
                                .setDurationDemo(dto.getDurationDemo())
                                .setDurationPayed(dto.getDurationPayed())
                                .setArchive(dto.isArchive()));

                return Optional.of(tariffRepository.save(businessTariffEntity).getDto());

        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<PaymentOrderDetail>> getAllPaymentOrderDetails(@NotNull Long serviceId) {
        return paymentDetailsRepo.allPaymentOrderDetails(serviceId);
    }

    @Override
    public Optional<PaymentOrderDetail> getPaymentOrderDetailsForOneTariff(@NotNull Long tariffId) {
        return paymentDetailsRepo.getPaymentOrderDetailByBusinessTariffId(tariffId);
    }

    @Override
    public Optional<PaymentOrderDetail> addOrEditPaymentOrderDetails(PaymentOrderDetailsRequest request) {
        Optional<PaymentOrderDetail> detail = paymentDetailsRepo.getPaymentOrderDetailByBusinessTariffId(request.tariff);
        if (detail.isPresent()) { // проверяем есть ли запись по переданному тарифу
            Optional<BusinessTariffEntity> tariffEntity = tariffRepository.findById(request.tariff);
            if (tariffEntity.isPresent()) { // а такой тариф вообще есть в CRM?
                PaymentOrderDetail updatedDetail = paymentDetailsRepo.saveAndFlush(detail.get().$update(request, tariffEntity.get()));
                log.info("Успешно обновлены платежные реквизиты для записи с Id {}", updatedDetail.getId());
                return Optional.of(updatedDetail);
            } else { //если такого тарифа не существует вообще в CRM
                log.error("Ошибка при обновлении платежных реквизитов для тарифа. Тарифа с id {} не существует!", request.tariff);
                return Optional.empty();
            }
        } else { // если записей нет, добавляем новую

            Optional<BusinessTariffEntity> tariffEntity = tariffRepository.findById(request.tariff);
            if (tariffEntity.isPresent()) { // а такой тариф вообще есть в CRM?
                PaymentOrderDetail newDetail = paymentDetailsRepo.saveAndFlush(PaymentOrderDetail.builder()
                        .amount(request.amount)
                        .bankName(request.bankName)
                        .bik(request.bik)
                        .businessTariff(tariffEntity.get())
                        .consumerAccountNumber(request.accountNumber)
                        .consumerKpp(request.kpp)
                        .consumerName(request.name)
                        .corrAccount(request.corrAccount)
                        .description(request.description)
                        .inn(request.inn)
                        .build());

                log.info("Успешно добавлены платежные реквизиты для тарифа с Id {}. Id новой записи: {}",
                        request.tariff, newDetail.getId());
                return Optional.of(newDetail);
            } else {
                log.error("Ошибка при добавлении платежных реквизитов для тарифа. Тарифа с id {} не существует!", request.tariff);
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<List<PaymentOrderDetail>> deletePaymentOrderDetails(@NotNull Long detailId) {
        Optional<PaymentOrderDetail> detail = paymentDetailsRepo.getPaymentOrderDetailById(detailId);
        if (detail.isPresent()) { // проверяем есть ли запись по переданному id
            paymentDetailsRepo.deleteById(detailId);
            return Optional.of(paymentDetailsRepo.findAll());
        } else { // если нет, то...
            log.error("Ошибка при удалении платежных реквизитов. Нет записей с id {} !", detailId);
            return Optional.empty();
        }
    }
}
