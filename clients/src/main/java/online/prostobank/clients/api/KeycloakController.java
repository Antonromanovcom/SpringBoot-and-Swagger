package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

import static online.prostobank.clients.api.ApiConstants.*;

@Benchmark
@JsonLogger
@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = ACCOUNT_APPLICATION_CONTROLLER,
        produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class KeycloakController {

    private final KeycloakAdminClient adminClient;

    @ApiOperation(value = "Получение списка пользователей Keycloak")
    @GetMapping(GET_ALL_KEYCLOAK_USERS)
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUsers() {
        return new ResponseEntity<>(
                ResponseDTO.goodResponse(ACCEPTED, adminClient.getUsersCache()),
                HttpStatus.OK
        );
    }

    @ApiOperation(value = "Получение списка пользователей Keycloak по параметру")
    @GetMapping(GET_KEYCLOAK_USER_BY_USERNAME)
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUserBy(
            @NotEmpty(message = "Параметр не может быть пустым") @RequestParam String username
    ) {
        return new ResponseEntity<>(
                ResponseDTO.goodResponse(ACCEPTED, adminClient.getUserByUsername(username)),
                HttpStatus.OK
        );
    }

}
