package online.prostobank.clients.api.business;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.business.ClientTariffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static online.prostobank.clients.api.ApiConstants.ACCEPTED;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/business/client/tariff")
public class BusinessClientController {

    private final ClientTariffService clientTariffService;

    @ApiOperation(value = "Получение списка тарифов клиента")
    @GetMapping("")
    public ResponseEntity<ResponseDTO> getAllTariffClient(@RequestParam Long clientId){
        return new ResponseEntity<>(
                clientTariffService.getInfoClientTariff(clientId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Нет тарифов")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Получение списка тарифов клиента по определенному сервису")
    @GetMapping("/service")
    public ResponseEntity<ResponseDTO> getAllTariffClient(@RequestParam Long serviceId, @RequestParam Long clientId){
        return new ResponseEntity<>(
                clientTariffService.getInfoClientServiceTariff(serviceId, clientId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Тарифов нет")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Установка тарифа в демо")
    @PostMapping(value = "/demo", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> setDemo(@RequestParam Long clientId, @RequestParam Long tariffId){
        return new ResponseEntity<>(
                clientTariffService.setDemo(clientId, tariffId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Ошибка установки в демо")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Установка тарифа в оплачено")
    @PostMapping(value = "/pay", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> setPay(@RequestParam Long clientId, @RequestParam Long tariffId){
        return new ResponseEntity<>(
                clientTariffService.setPay(clientId, tariffId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Ошибка оплаты")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Установка даты активации для тарифа")
    @PostMapping(value = "/activationdate", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> setActivationDate(@RequestParam String clientKeyCloakId, @RequestParam Long tariffId, @RequestParam Long dateInMillsec){
        log.info("Попытка установить дату активации тарифа извне (KeyCloak Id: {} | Tariff Id: {}| Date In Milliseconds: {})", clientKeyCloakId, tariffId, dateInMillsec);
        return new ResponseEntity<>(
                clientTariffService.setActivationDate(clientKeyCloakId, tariffId, dateInMillsec)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Ошибка установки даты активации для тарифа")),
                HttpStatus.OK
        );
    }


    @ApiOperation(value = "Установка даты первого захода")
    @GetMapping("/first/entrance")
    public ResponseEntity<ResponseDTO> setFirstEntranceTime(@RequestParam Long clientId, @RequestParam Long tariffId, @RequestParam Long dateInMillsec) {
        return new ResponseEntity<>(
                clientTariffService.setFirstEntrance(clientId, tariffId, dateInMillsec)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Ошибка установки даты первого входа")),
                HttpStatus.OK
        );
    }

}
