package online.prostobank.clients.domain;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.EmailDuplicateException;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static online.prostobank.clients.utils.AutowireHelper.autowire;
import static online.prostobank.clients.utils.Utils.COMMA_DELIMITER;

@Slf4j
@Embeddable
public class ClientValue extends ClientValueEntity {

    @Transient
    @Autowired
    private ClientValueService clientValueService;

    public ClientValue(@Nonnull String phone) {
        super(phone);
    }

    public ClientValue(
            @Nonnull String name,
            @Nonnull String email,
            @Nonnull String phone,
            @Nonnull String inn,
            @Nonnull String ogrn,
            @Nonnull String head) {
        super(name, email, phone, inn, ogrn, head);
        setInnOgrn(StringUtils.trimToNull(inn), StringUtils.trimToNull(ogrn));

    }

    public ClientValue() {
    }

    private String getSpecificWordFromFIO(@Nonnull String fio, int num) {

        if (StringUtils.isBlank(fio)) {
            return "";
        }
        String[] separatedFio = fio.split(" ");
        if (separatedFio.length > num || num == 0) {
            return separatedFio[num];
        }
        if (separatedFio.length < num) return separatedFio[0];
        return separatedFio[num - 1];
    }

    public String getFirstName() { // имя
        if (isSP()) {
            if (StringUtils.isBlank(this.getName())) {
                return "";
            }
            return getSpecificWordFromFIO(this.getName(), 2);
        } else {
            if (StringUtils.isBlank(this.getHead())) {
                return "";
            }
            return getSpecificWordFromFIO(this.getHead(), 1);
        }
    }


    public String getSurname() { // фамилия
        if (isSP()) {
            if (StringUtils.isBlank(this.getName())) {
                return "";
            }
            return getSpecificWordFromFIO(this.getName(), 1);
        } else {
            if (StringUtils.isBlank(this.getHead())) {
                return "";
            }
            return getSpecificWordFromFIO(this.getHead(), 0);
        }
    }

    public String getSecondName() { // отчество
        if (isSP()) {
            return getSpecificWordFromFIO(this.getName(), 3);
        } else {
            return getSpecificWordFromFIO(this.getHead(), 2);
        }
    }

    /**
     * Правки email
     */
    public boolean editEmail(String newEmail, boolean needToValidateEmptyEmail, boolean needToValidateEmailDuplicates)
            throws EmailDuplicateException, IllegalArgumentException {
        log.info("Trying to change e-mail to '{}'", newEmail);
		if (clientValueService == null) {
			autowire(this);
		}
        if (clientValueService.isPossibleToChangeEmail(
                getEmail(),
                newEmail,
                needToValidateEmptyEmail,
                needToValidateEmailDuplicates)
        ) {
            this.setEmail(newEmail);
            log.info("Email has been changed to '{}'", this.getEmail());
            return true;
        }
        log.warn("Trying to change e-mail to '{}'. Was not successfully", newEmail);
        return false;
    }

    public boolean editPhone(String newPhone) {
        newPhone = StringUtils.trim(newPhone);
        if (StringUtils.isEmpty(newPhone)) {
            throw new IllegalArgumentException("newPhone can't be null");
        }
        newPhone = normalizePhone(newPhone);
        if (this.getPhoneRaw() == null || !this.getPhoneRaw().equals(newPhone)) {
            this.setPhone(newPhone);
            return true;
        }
        return false;
    }

    /**
     * Добавление адреса на форме холодной заявки
     *
     * @param newAddress
     * @return
     */
    public boolean editAddress(String newAddress) {
        newAddress = StringUtils.trim(newAddress);
        if (StringUtils.isEmpty(newAddress)) {
            throw new IllegalArgumentException("Адрес не может быть пустым");
        }
        if (StringUtils.isEmpty(this.getAddress()) || !this.getAddress().equals(newAddress)) {
            this.setAddress(newAddress);
            return true;
        }
        return false;
    }

    /**
     * Добавление ИНН для формы холодной заявки
     *
     * @param taxNumber - ИНН
     * @return
     */
    public boolean editTaxNumberColdApplication(String taxNumber) {
        taxNumber = StringUtils.trim(taxNumber);
        if (StringUtils.isEmpty(taxNumber)) {
            throw new IllegalArgumentException("ИНН не может быть пустым");
        }
        if (StringUtils.isEmpty(this.getInn())) {
            return setInnOgrn(taxNumber);
        }
        return false;
    }

    /**
     * Ввод ИНН для заявки в раннем статусе. Если человек не довёл свою заявку до конца и указал только данные для связи с ним
     *
     * @return
     */
    boolean enterInn(String inn, boolean needToValidateEmptyTaxNumber) {
        if (StringUtils.isEmpty(inn) && needToValidateEmptyTaxNumber) {
            throw new IllegalArgumentException("ИНН не может быть пустым");
        }

        if (StringUtils.isEmpty(this.getInn())) {
            setInnOgrn(inn);
            return true;
        }
        return false;
    }

    /**
     * Обновление кодов из контура
     *
     * @param primaryCodes
     * @param secondaryCodes
     */
    public void setCodes(String[] primaryCodes, String[] secondaryCodes) {
        if (primaryCodes != null) {
            this.setPrimaryCodes(String.join(COMMA_DELIMITER, primaryCodes));
        }

        if (secondaryCodes != null) {
            this.setSecondaryCodes(String.join(COMMA_DELIMITER, secondaryCodes));
        }
    }

    /**
     * Устанавливаю чёрные оквэды и рискованные оквэды, если есть
     */
    public void setCodesFromKonturModel(String blackListedCodes, String riskyCodes) {
        if (blackListedCodes == null) {
            this.setBlackListedCodes("");
        } else {
            this.setBlackListedCodes(blackListedCodes);
        }

        if (riskyCodes == null) {
            this.setRiskyCodes("");
        } else {
            this.setRiskyCodes(riskyCodes);
        }
    }

    /**
     * Устанавливает имя при заполнении из холодной заявки
     *
     * @param name
     */
    public boolean editName(String name) {
        name = StringUtils.trim(name);
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (StringUtils.isNotBlank(name)) {
            this.setName(name);
            return true;
        }
        return false;
    }

    private static int getNumericValueInPosition(String taxNumber, int i) {
        return Character.getNumericValue(taxNumber.charAt(i));
    }

    void updateInfo(String name, String shortName, String headName, String address) {
        this.setName(
                StringUtils.isNotBlank(name)
                        ? name
                        : this.getName()
        ); // наименование клиента (организация) ФИО для ИП
        this.setShortName(
                StringUtils.isNotBlank(shortName)
                        ? shortName
                        : this.getShortName()
        ); // наименование клиента (организация) ФИО для ИП
        this.setHead(
                StringUtils.isNotBlank(headName)
                        ? headName
                        : this.getHead()
        ); // глава организации
        this.setAddress(
                StringUtils.isNotBlank(address)
                        ? address
                        : this.getAddress()
        );
    }

    /**
     * Сохранение номера в поле ИНН или ОГРН в зависимости от длинны номера
     *
     * @param val
     */
    public boolean setInnOgrn(String val) {
        val = StringUtils.trim(val);
        if (StringUtils.isNotBlank(val)) {
            if (TaxNumberUtils.isInnValid(val)) {
                this.setInn(val);
                return true;
            } else if (TaxNumberUtils.isOgrnValid(val)) {
                this.setOgrn(val);
                return true;
            }
        }
        return false;
    }

    /**
     * Валидация ИНН, в случае успеха, сохранение промокода (реферального ИНН)
     * @param promoInn
     * @return
     */
    public boolean setPromoInn(String promoInn) {
        if (TaxNumberUtils.isInnValid(promoInn)) {
            this.setPromoInnRaw(promoInn);
            return true;
        }
        return false;
    }

    /**
     * Phone helper
     *
     * @param phone
     * @return
     */
    public static String normalizePhone(String phone) {
        try {
            return phone == null
                    ? ""
                    : "" + PhoneNumberUtil.getInstance().parse(phone, "RU").getNationalNumber();
        } catch (NumberParseException ex) {
            log.error("Problem parsing phone " + phone);
            return phone;
        }
    }

    public final void setInnOgrn(String inn, String ogrn) {
        if (TaxNumberUtils.isInnValid(inn)) {
            this.setInn(inn.trim());
        }

        if (TaxNumberUtils.isOgrnValid(ogrn)) {
            this.setOgrn(ogrn.trim());
        }
    }

    public String getPhone() {
        return normalizePhone(this.getPhoneRaw());
    }

    private List<String> getBlackAndRiskyCodesAsList() {
        List<String> result = new ArrayList<>();
        String black = this.getBlackListedCodes();
        String risky = this.getRiskyCodes();
        if (StringUtils.isNotEmpty(black)) {
            result.addAll(Arrays.asList(black.split(COMMA_DELIMITER)));
        }
        if (StringUtils.isNotEmpty(risky)) {
            result.addAll(Arrays.asList(risky.split(COMMA_DELIMITER)));
        }
        return result;
    }
}
