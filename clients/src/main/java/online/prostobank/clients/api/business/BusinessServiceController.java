package online.prostobank.clients.api.business;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.business.ServiceBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static online.prostobank.clients.api.ApiConstants.ACCEPTED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business/service")
public class BusinessServiceController {

    private final ServiceBusiness serviceBusinessService;

    @ApiOperation(value = "Получение списка сервисов")
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllService() {
        return new ResponseEntity<>(
                serviceBusinessService.getAllServices()
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Нет сервисов")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Получение списка сервисов по клиенту")
    @GetMapping("/client/all")
    public ResponseEntity<ResponseDTO> getAllClientServices(Long clientId) {
        return new ResponseEntity<>(
                serviceBusinessService.userServices(clientId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Нет сервисов")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Получение информации по сервису")
    @GetMapping("")
    public ResponseEntity<ResponseDTO> getService(Long serviceId) {
        return new ResponseEntity<>(
                serviceBusinessService.getService(serviceId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Нет сервиса")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Изменение/сохранение сервиса")
    @PostMapping(value = "/save", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> save(@RequestBody BusinessServiceDTO dto) {
        return new ResponseEntity<>(
                serviceBusinessService.saveService(dto)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Ошибка сохранения сервиса")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Включение сервиса у пользователя")
    @PostMapping(value = "/client/turn_on", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> turnOn(Long clientId, Long serviceId) {
        return new ResponseEntity<>(
                serviceBusinessService.turnOn(clientId, serviceId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно включить сервис")),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Отключение сервиса у пользователя")
    @PostMapping(value = "/client/turn_off", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> turnOff(Long clientId, Long serviceId) {
        return new ResponseEntity<>(
                serviceBusinessService.turnOff(clientId, serviceId)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно отключить сервис")),
                HttpStatus.OK
        );
    }

}
