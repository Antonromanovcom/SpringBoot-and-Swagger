package online.prostobank.clients.services.anketa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.AccountApplicationDTO;
import online.prostobank.clients.connectors.exceptions.AnticorruptionException;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.PersonValue;
import online.prostobank.clients.domain.repository.CityRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Optional;

import static online.prostobank.clients.utils.Utils.DD_MM_YYYY_FORMATTER;
import static online.prostobank.clients.utils.Utils.UNKNOWN_CITY;

/**
 * Транслирует данные извне в данные доменной модели
 * http://wiki.c2.com/?AnticorruptionLayer
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class Anticorruption implements IAnticorruption {

    private final CityRepository cityRepository;

	@Nonnull
    @Override
    public City obtainCity(String city) {
        return Optional.ofNullable(city)
                .map(s ->
                        cityRepository
                                .findByNameIgnoreCase(city)
                                .orElseGet(() -> {
                                    throw new AnticorruptionException(
                                            String.format("город с именем %s не найден", city)
                                    );
                                })
                ).orElseGet(
                        () ->
                                cityRepository
                                        .findByNameIgnoreCase(UNKNOWN_CITY)
                                        .orElseGet(() -> {
                                            throw new AnticorruptionException(
                                                    "Нет пустого города"
                                            );
                                        })
                        );
    }

    @Nonnull
    @Override
    public ClientValue obtainClient(String phone) {
        if (phone == null) throw new AnticorruptionException("phone == null");
        String normalizePhone = ClientValue.normalizePhone(phone);
        return new ClientValue("" + normalizePhone);
    }

    @Override
    public void fillPerson(PersonValue pv, AccountApplicationDTO dto) {
        log.info("Заполнение информации о паспорте для заявки с ИНН '{}' и номером телефона '{}'. {}", dto.getContactInfo().getInnOrOgrn(), dto.getContactInfo().getPhone(), dto.getPassportInfo());
        if (dto.getPassportInfo() != null) {
            pv.setSer(dto.getPassportInfo().passport.split("-")[0]);
            pv.setNum(dto.getPassportInfo().passport.split("-")[1]);
            pv.setDob(LocalDate.parse(dto.getPassportInfo().birthday, DD_MM_YYYY_FORMATTER));
            pv.setDoi(LocalDate.parse(dto.getPassportInfo().issueDate, DD_MM_YYYY_FORMATTER));
            pv.setIssuer(dto.getPassportInfo().issueBy);
            pv.setIssuerCode(dto.getPassportInfo().divisionCode);
            pv.setPob(dto.getPassportInfo().placeOfBirth);
        }
    }
}
