package online.prostobank.clients.api.state;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.services.state.StateMachineServiceI;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static online.prostobank.clients.api.ApiConstants.*;

@Benchmark
@JsonLogger
@RequiredArgsConstructor
@RestController
@RequestMapping(value = API_STATE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class StateController {

    private final StateMachineServiceI stateMachineService;

    @ApiOperation(value = "Возможные переходы статусов")
    @GetMapping(value = API_STATE_AVAILABLE)
    public ResponseEntity<ResponseDTO> getAvailableStates(@RequestParam Long idClient) {
        return new ResponseEntity<>(
                stateMachineService.getInfoNextStatus(idClient)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно просмотреть возможные переходы статусов")),
                HttpStatus.OK);

    }

    @ApiOperation(value = "Установить статус")
    @PostMapping(value = "/set", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<ResponseDTO> setState(@RequestBody StateSetterDTO dto) {
        return new ResponseEntity<>(
                stateMachineService.setState(dto)
                        .map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
                        .orElseGet(() -> ResponseDTO.badResponse("Невозможно установить статус")),
                HttpStatus.OK
        );
    }

}
