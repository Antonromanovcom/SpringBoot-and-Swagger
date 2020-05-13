package online.prostobank.clients.api.account;

import online.prostobank.clients.api.dto.FileDTO;
import online.prostobank.clients.api.dto.LKDemandDTO;
import online.prostobank.clients.api.dto.RequisitesDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.events.DocumentDownloaded;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.services.StorageException;
import online.prostobank.clients.utils.Utils;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static online.prostobank.clients.api.ApiConstants.API_ROOT;

/**
 *
 * @author yurij
 */
@Benchmark
@JsonLogger
@RestController
@RequestMapping(API_ROOT)
@Transactional
public class AccountEndpoint {
    private AccountApplicationRepository accountApplicationRepository;
    private ApplicationEventPublisher bus;

    @Autowired
    public AccountEndpoint(AccountApplicationRepository accountApplicationRepository, ApplicationEventPublisher bus) {
        this.accountApplicationRepository = accountApplicationRepository;
        this.bus = bus;
    }

    @RequestMapping(value = "clients/requisites", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public ResponseEntity<RequisitesDTO> requisites(
            @NotNull(message = "Параметр не задан") @RequestParam("personal_account") String requisites
    ) {
        RequisitesDTO answer = new RequisitesDTO();
        AccountApplication aa = accountApplicationRepository.findByAccountAccountNumber(requisites);
        if(aa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        answer.setRequisites(aa.getAccount().getAccountNumber());
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    /**
     * Поиск данных по заявке
     * @param requisites
     * @return
     */
    @RequestMapping(value = "clients/demands/{hashedId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public ResponseEntity<LKDemandDTO> demandByHashedId(
            @NotNull(message = "Параметр не задан") @PathVariable("hashedId") String requisites
    ) {
        Optional<AccountApplication> aa = accountApplicationRepository.findByLoginURLAndActive(requisites, true);
        if(!aa.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        LKDemandDTO dto = new LKDemandDTO();
        // TODO: зарефакторить возвращение нужный данных
        dto.company.type = aa.get().getClient().isLlc() ? "LLC" : "SP";
        dto.demand.requisites = aa.get().getAccount().getAccountNumber();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Поиск данных по заявке
     * @param requisites
     * @return
     */
    @RequestMapping(value = "clients/{hashedId}/login",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public ResponseEntity demandByHashedIdLogin(
            @NotNull(message = "Параметр не задан") @PathVariable("hashedId") String requisites
    ) {
        return new ResponseEntity( HttpStatus.OK);
    }

    /**
     * Подгрузка документов
     *
     * @param hashedId
     * @param file
     * @return
     */
    @RequestMapping(value = "clients/documents/{hashedId}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public ResponseEntity<String> demandByHashedId(
            @NotNull(message = "Параметр не задан") @PathVariable("hashedId") String hashedId,
            @RequestBody FileDTO file
    ) throws IOException {
        Optional<AccountApplication> aa = accountApplicationRepository.findByLoginURLAndActive(hashedId, true);
        if(!aa.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AccountApplication application = aa.get();
        if (file == null || file.encodedImage.getBytes().length == 0) {
            return new ResponseEntity<>("Файл не может быть пустым", HttpStatus.BAD_REQUEST);
        }
        byte[] fileAsBytes = Base64.decode(file.encodedImage.getBytes());
        if (fileAsBytes.length == 0) {
            return new ResponseEntity<>("Повреждённый файл", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(file.fileName) && StringUtils.isBlank(file.extension)) {
            return new ResponseEntity<>("Не указано имя файла", HttpStatus.BAD_REQUEST);
        }
        String mimeType = Utils.getMimeType(fileAsBytes);
        if (StringUtils.isBlank(mimeType)) {
            mimeType = Utils.getMimeTypeByFileName(file.fileName + '.' + file.extension);
        }
        AttachmentFunctionalType functionalType = AttachmentFunctionalType.getByStringKey(file.type);
        try {
            application.addAttachmentUnique(file.fileName, fileAsBytes, mimeType, functionalType);
            bus.publishEvent(new DocumentDownloaded(application, false, functionalType));
            return new ResponseEntity<>("Документ успешно загружен", HttpStatus.OK);
        } catch (StorageException ex) {
            return new ResponseEntity<>("Документ не удалось загрузить", HttpStatus.I_AM_A_TEAPOT);
        }
    }

    /**
     * Получение имен существующих пользовательских документов
     */
    @GetMapping(value = "clients/documents/names/{hashedId}")
    public List<String> getAllAttachmentsNames(@PathVariable("hashedId") String hashedId) {
        return accountApplicationRepository.findByLoginURLAndActive(hashedId, true)
                .map(AccountApplication::getAttachments)
                .map(attachments ->
                        attachments.stream()
                                .map(Attachment::getAttachmentName)
                                .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);
    }
}
