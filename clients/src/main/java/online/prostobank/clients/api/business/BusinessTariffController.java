package online.prostobank.clients.api.business;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.business.TariffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import static online.prostobank.clients.api.ApiConstants.ACCEPTED;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/business/tariff")
public class BusinessTariffController {
    private final TariffService tariffService;

    @ApiOperation(value = "Получение списка тарифов по сервису")
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllTariff(Long serviceId) {
        return new ResponseEntity<>(
                tariffService.getListTariffByServiceId(serviceId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Нет тарифов")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Изменение/сохранение тарифа")
    @PostMapping(value = "/save", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> save(@RequestBody BusinessTariffDTO dto) {
        return new ResponseEntity<>(
                tariffService.saveTariff(dto)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно изменить/сохранить тариф")),
                HttpStatus.OK
        );
    }

    //  ------------- БЛОК МЕТОДОВ ДЛЯ CRUD'А ПО РЕКВИЗИТАМ ДЛЯ ПЛАТЕЖЕК НА ОПЛАТУ ТАРИФОВ/УСЛУГ -----------------------

    @ApiOperation(value = "Платежные реквизиты по всем тарифам")
    @GetMapping(value = "/payment_detail/all", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> getAllPaymentOrderDetails(Long serviceId) {
    log.info("Попытка получить реквизиты всех тарифов по сервису(Service Id: {})", serviceId);
        return new ResponseEntity<>(
                tariffService.getAllPaymentOrderDetails(serviceId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно получить реквизиты на оплату услуг")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Платежные реквизиты по конкретному тарифу")
    @GetMapping(value = "/payment_detail", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> getPaymentOrderDetailsForOneTariff(Long tariffId) {
        log.info("Попытка получить реквизиты по тарифу: {}", tariffId);
        return new ResponseEntity<>(
                tariffService.getPaymentOrderDetailsForOneTariff(tariffId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно получить реквизиты на оплату услуг")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Добавить новые платежные реквизиты по конкретному тарифу или обновить имеющиеся")
    @PostMapping(value = "/payment_detail", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> savePaymentDetail(@Valid @RequestBody PaymentOrderDetailsRequest payLoad) {
        log.info("Попытка добавить новые платежные реквизиты: {}", payLoad);
        return new ResponseEntity<>(
                tariffService.addOrEditPaymentOrderDetails(payLoad)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно добавить/обновить реквизиты на оплату услуг")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Удалить платежные реквизиты по конкретному тарифу")
    @DeleteMapping(value = "/payment_detail", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> savePaymentDetail(Long detailId) {
        log.info("Попытка удалить платежные реквизиты по id: {}", detailId);
        return new ResponseEntity<>(
                tariffService.deletePaymentOrderDetails(detailId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно удалить реквизиты на оплату услуг")),
                HttpStatus.OK
        );
    }




}
