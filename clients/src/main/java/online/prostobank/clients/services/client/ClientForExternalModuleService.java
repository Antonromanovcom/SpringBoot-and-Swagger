package online.prostobank.clients.services.client;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.rest.ClientValueDTO;
import online.prostobank.clients.domain.repository.ClientForExternalModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.Optional;

/**
 * Сервис для предоставления сведений о клиенте модулю №2 (PUSH)
 */
@Slf4j
@Service
public class ClientForExternalModuleService {
    private final ClientForExternalModuleRepository repository;

    @Autowired
    public ClientForExternalModuleService(ClientForExternalModuleRepository repository) {
        this.repository = repository;
    }

    /**
     * Получение сведений о клиенте по известному номеру счета
     * @param accountNumber -- номер банковскогосчета
     * @return
     */
    public Optional<ClientValueDTO> getClientInfoByAccount(@NotEmpty(message = "Не указан номер счета") String accountNumber) {
        log.info("Модулем №2 запрошены сведения по счету {}", accountNumber);
        accountNumber = accountNumber.replaceAll("[^\\d.]", "");
        Optional<ClientValueDTO> clientValue = repository.getClientInfoByAccount(accountNumber);
        clientValue.ifPresent(it ->log.info("Для модуля №2 найден клиент ИНН = {}", it.getInn()));
        return clientValue;
    }
}
