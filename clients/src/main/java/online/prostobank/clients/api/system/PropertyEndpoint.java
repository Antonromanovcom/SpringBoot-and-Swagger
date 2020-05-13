package online.prostobank.clients.api.system;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import online.prostobank.clients.domain.PropertyEntity;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static online.prostobank.clients.api.ApiConstants.CONFIG_CONTROLLER;

@Benchmark
@JsonLogger
@Slf4j
@RestController
@RequestMapping(
        value = CONFIG_CONTROLLER,
        produces ={MediaType.APPLICATION_JSON_UTF8_VALUE})
@Transactional
public class PropertyEndpoint {

    private final DbPropertiesServiceI service;

    public PropertyEndpoint(@Nonnull DbPropertiesServiceI service) {
        this.service = service;
    }

    @GetMapping(value = "get_property_by_key")
    @ResponseBody
    public ResponseEntity<PropertyEntity> getPropertyEntityByKey(
            @NotEmpty(message = "Параметр не может быть пустым") @RequestParam(name = "key") String key
    ) throws PropertyServiceException {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getPropertyByKey(key));

    }

    @GetMapping("submit_property")
    @ResponseBody
    public ResponseEntity<PropertyEntity> addPropertyEntity(
            @NotEmpty(message = "Параметр не может быть пустым")  @RequestParam(name = "key") String key,
            @NotNull(message = "Параметр не задан")               @RequestParam(name = "value") String value) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(service.submitNewProperty(key, value));
        } catch (PropertyServiceException e) {
            log.error("Unable to add property, cause :: {}",
                    ExceptionUtils.getRootCause(e)
            );
            return ResponseEntity.badRequest().body(new PropertyEntity());
        }
    }

}
