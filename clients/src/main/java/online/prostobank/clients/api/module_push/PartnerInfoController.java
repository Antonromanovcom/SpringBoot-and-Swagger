package online.prostobank.clients.api.module_push;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.avro.module_push.UserDto;
import online.prostobank.clients.api.dto.rest.ClientValueDTO;
import online.prostobank.clients.services.StorageException;
import online.prostobank.clients.services.attacment.AttachmentService;
import online.prostobank.clients.services.client.ClientForExternalModuleService;
import online.prostobank.clients.utils.HumanNamesFromCompany;
import online.prostobank.clients.utils.Utils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * API для Модуля №2 (PUSH) для получения ограниченных сведений о клиенте
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(value="Module #2 API controller", description="Получение сведений о клиенте для Модуля №2")
public class PartnerInfoController {
    //по мере развития приложения вместо константы будут выбираться реальные партнеры
    private final static String DEFAULT_PARTNER_NAME = "PROSTOBANK";
    private final ClientForExternalModuleService clientForExternalModuleService;
    private final AttachmentService attachmentService;

    /**
     * Сведения о клиенте по номеру счета. Принимает и возвращает один и тот же avro-объект. Неопределенные поля заполняются null.
     * Сериализация application/avro+json
     * @param userDtoSource
     * @param authentication
     * @param response
     * @return
     */
    //TODO: большой костыль -- userDtoSource на самом деле объект avro, но по какой-то причине возникает ошибка сравнения типов после его десериализации
    //см. комменты к https://context.atlassian.net/browse/APIKUB-1931
    @ApiOperation(value = "Сведения о клиенте по номеру его счета", httpMethod = "POST",
            consumes = "application/avro+json", produces = "application/avro+json", response = UserDto.class)
    @RequestMapping(value = "/api/external/module/2", method = RequestMethod.POST, produces = "application/avro+json", consumes = "application/avro+json")
    public UserDto getClientValueAvroJson(
            @ApiParam(value = "Avro UserDto", required = true) @RequestBody(required = false) LinkedHashMap userDtoSource,
            Authentication authentication, HttpServletResponse response) {
        UserDto userDto = new UserDto();
        userDto.setClientAccountNumber((String)userDtoSource.get("clientAccountNumber"));
        return getClientValue(userDto, response);
    }

    /**
     * Сведения о клиенте по номеру счета. Принимает и возвращает один и тот же avro-объект. Неопределенные поля заполняются null.
     * Сериализация application/avro
     * @param userDto
     * @param authentication
     * @param response
     * @return
     */
    @ApiOperation(value = "Сведения о клиенте по номеру его счета", httpMethod = "POST",
            consumes = "application/avro", produces = "application/avro", response = UserDto.class)
    @RequestMapping(value = "/api/external/module/2", method = RequestMethod.POST, produces = "application/avro", consumes = "application/avro")
    public UserDto getClientValueAvroBinary(
            @ApiParam(value = "Avro UserDto", required = true) @RequestBody(required = false) UserDto userDto,
            Authentication authentication, HttpServletResponse response) {
        return getClientValue(userDto, response);
    }

    @RequestMapping(value = "/api/external/module/2/{userId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String checkStorage(@PathVariable Long userId) {
        try {
            attachmentService.getBankAttachments(userId);
            attachmentService.getUserAttachments(userId);
            return "OK";
        } catch (StorageException ex) {
            return ex.getMessage();
        }
    }

    private UserDto getClientValue(UserDto userDto, HttpServletResponse response) {
        if (userDto == null) {
            log.error("От модуля №2 поступил запрос с пустым параметром UserDto");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (userDto.getClientAccountNumber() == null) {
            log.error("От модуля №2 поступил запрос с null значением номера счета");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        String accountNumber = userDto.getClientAccountNumber().toString();
        Optional<ClientValueDTO> clientOptional = clientForExternalModuleService.getClientInfoByAccount(accountNumber);

        if (clientOptional.isPresent()) {
            UserDto result = new UserDto();
            ClientValueDTO clientValueDTO = clientOptional.get();
            String ogrn = clientValueDTO.getOgrn();
            String inn = clientValueDTO.getInn();
            boolean isLegalEntity = Utils.isLegalEntity(inn, ogrn);
            HumanNamesFromCompany.Names names = HumanNamesFromCompany.createNames(clientValueDTO.getName(), clientValueDTO.getHead(), isLegalEntity);
            result.setClientAccountNumber(accountNumber);
            result.setFirstName(names.firstName());
            result.setMiddleName(names.middleName());
            result.setLastName(names.lastName());
            result.setPartner(DEFAULT_PARTNER_NAME);
            return result;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }
}
