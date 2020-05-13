package online.prostobank.clients.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class TaxNumberUtils {
    public static final int UL_INN_LENGTH = 10;
    public static final int IP_INN_LENGTH = 12;

    public static final int OGRN_LENGTH = 13;
    public  static final int IP_OGRN_LENGTH = 15;

    /**
     * Результат проверки ИНН ЮЛ или ИНН ИП
     */
    public enum InnValidationResult {
        Ok(null),
        DigitsOnly("ИНН должен состоять только из цифр"),
        InvalidLength("ИНН должен содержать 10 либо 12 цифр"),
        InvalidIpCheckDigit("Неправильное контрольное число для ИНН ИП"),
        InvalidUlCheckDigit("Неправильное контрольное число для ИНН ЮЛ"),
        Unknown("Неизвестная ошибка");

        private String result;

        InnValidationResult(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }
    }

    /**
     * Результат проверки ОГРН или ОГРНИП
     */
    public enum OgrnValidationResult {
        Ok(null),
        DigitsOnly("ОРГН должен состоять только из цифр"),
        InvalidLength("ОГРН должен содержать 13 либо 15 цифр"),
        InvalidIpCheckDigit("Неправильное контрольное число для ОГРНИП"),
        InvalidUlCheckDigit("Неправильное контрольное число для ОГРН"),
        Unknown("Неизвестная ошибка");

        private String result;

        OgrnValidationResult(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }
    }

    /**
     * Проверяет ИНН ИП или ИНН ЮЛ на правильность формирования
     *
     * @param innString Строка с ИНН
     * @return Результат проверки с описанием
     */
    public static InnValidationResult isInnValidResult(String innString) {
        innString = StringUtils.trim(innString);

        if (!StringUtils.isNumeric(innString)) {
            return InnValidationResult.DigitsOnly;
        }

        if (!isInnLengthValid(innString)) {
            return InnValidationResult.InvalidLength;
        }

        if (innString.length() == UL_INN_LENGTH) {
            if (!isUlInnCheckDigitValid(innString)){
                return InnValidationResult.InvalidUlCheckDigit;
            }

            return InnValidationResult.Ok;
        }

        if (innString.length() == IP_INN_LENGTH) {
            if (!isIpInnCheckDigitValid(innString)) {
                return InnValidationResult.InvalidIpCheckDigit;
            }

            return InnValidationResult.Ok;
        }

        return InnValidationResult.Unknown;
    }

    /**
     * Проверяет ИНН ИП или ИНН ЮЛ на правильность формирования
     *
     * @param innString Строка с ИНН
     * @return Является ли строка корректным ИНН ЮЛ или ИНН ИП
     */
    public static boolean isInnValid(String innString) {
        InnValidationResult result = isInnValidResult(innString);
        return result == InnValidationResult.Ok;
    }

    /**
     * Проверяет ОГРН и ОГРНИП на правильность формирования
     * @param ogrnString Строка с ОГРН или ОГРНИП
     * @return Является ли строка корректным ОГРН или ОГРНИП
     */
    public static boolean isOgrnValid(String ogrnString) {
        OgrnValidationResult result = isOgrnValidResult(ogrnString);
        return result == OgrnValidationResult.Ok;
    }

    /**
     * Проверяет ОГРН или ОРГНИП на правильность формирования
     *
     * @param ogrnString Строка с ОГРН
     * @return Результат проверки с описанием
     */
    public static OgrnValidationResult isOgrnValidResult(String ogrnString) {
        ogrnString = StringUtils.trim(ogrnString);

        if (!StringUtils.isNumeric(ogrnString)) {
            return OgrnValidationResult.DigitsOnly;
        }

        if (!isOgrnLengthValid(ogrnString)) {
            return OgrnValidationResult.InvalidLength;
        }

        if (ogrnString.length() == OGRN_LENGTH) {
            if (!IsOgrnCheckDigitValid(ogrnString)){
                return OgrnValidationResult.InvalidUlCheckDigit;
            }

            return OgrnValidationResult.Ok;
        }

        if (ogrnString.length() == IP_OGRN_LENGTH) {
            if (!IsOgrnIpCheckDigitValid(ogrnString)){
                return OgrnValidationResult.InvalidIpCheckDigit;
            }

            return OgrnValidationResult.Ok;
        }

        return OgrnValidationResult.Unknown;
    }

    /**
     * Проверяет корректность длины ОГРН
     * @param ogrn ОГРН или ОГРНИП
     * @return Результат проверки
     */
    private static boolean isOgrnLengthValid(String ogrn) {
        return ogrn.length() == OGRN_LENGTH || ogrn.length() == IP_OGRN_LENGTH;
    }

    /**
     * Проверяет корректность длины ИНН
     * @param inn ИНН ИП или ИНН ЮЛ
     * @return Результат проверки
     */
    private static boolean isInnLengthValid(String inn){
        return inn.length() == UL_INN_LENGTH || inn.length() == IP_INN_LENGTH;
    }

    /**
     * Проверяет контрольную цифру ОГРН
     * @param ogrn ОГРН
     * @return Результат проверки
     */
    private static boolean IsOgrnCheckDigitValid(String ogrn) {
        long l = new BigDecimal(ogrn).divide(BigDecimal.TEN).longValue();
        long digit = Math.floorMod(l, 11);
        if (digit == 0) {
            digit = 10L;
        }
        return new Long(getNumericValueInPosition(ogrn, 12)).equals(digit);
    }

    /**
     * Проверяет контрольную цифру ОГРНИП
     * @param ogrnIp ОГРНИП
     * @return Результат проверки
     */
    private static boolean IsOgrnIpCheckDigitValid(String ogrnIp){
        long l = new BigDecimal(ogrnIp).divide(BigDecimal.TEN).longValue();
        long digit = Math.floorMod(l, 13);
        digit = Math.floorMod(digit, 10);
        return new Long(getNumericValueInPosition(ogrnIp, 14)).equals(digit);
    }

    /**
     * Проверяет контрольную цифру ИНН ЮЛ
     * @param innUl ИНН ЮЛ
     * @return Результат проверки
     */
    private static boolean isUlInnCheckDigitValid(String innUl){
        @SuppressWarnings("Duplicates") String digit = Integer.toString(((
                2 * getNumericValueInPosition(innUl, 0) +
                        4 * getNumericValueInPosition(innUl, 1) +
                        10 * getNumericValueInPosition(innUl, 2) +
                        3 * getNumericValueInPosition(innUl, 3) +
                        5 * getNumericValueInPosition(innUl, 4) +
                        9 * getNumericValueInPosition(innUl, 5) +
                        4 * getNumericValueInPosition(innUl, 6) +
                        6 * getNumericValueInPosition(innUl, 7) +
                        8 * getNumericValueInPosition(innUl, 8)) % 11) % 10);

        boolean result = getNumericValueInPosition(digit, 0) == getNumericValueInPosition(innUl, 9);
        return result;
    }

    /**
     * Проверяет контрольные цифры ИНН ИП
     * @param innIp ИНН ИП
     * @return Результат проверки
     */
    private static boolean isIpInnCheckDigitValid(String innIp){
        @SuppressWarnings("Duplicates") String digit11 = Integer.toString(((
                7 * getNumericValueInPosition(innIp, 0) +
                        2 * getNumericValueInPosition(innIp, 1) +
                        4 * getNumericValueInPosition(innIp, 2) +
                        10 * getNumericValueInPosition(innIp, 3) +
                        3 * getNumericValueInPosition(innIp, 4) +
                        5 * getNumericValueInPosition(innIp, 5) +
                        9 * getNumericValueInPosition(innIp, 6) +
                        4 * getNumericValueInPosition(innIp, 7) +
                        6 * getNumericValueInPosition(innIp, 8) +
                        8 * getNumericValueInPosition(innIp, 9)) % 11) % 10);

        @SuppressWarnings("Duplicates") String digit12 = Integer.toString(((
                3 * getNumericValueInPosition(innIp, 0) +
                        7 * getNumericValueInPosition(innIp, 1) +
                        2 * getNumericValueInPosition(innIp, 2) +
                        4 * getNumericValueInPosition(innIp, 3) +
                        10 * getNumericValueInPosition(innIp, 4) +
                        3 * getNumericValueInPosition(innIp, 5) +
                        5 * getNumericValueInPosition(innIp, 6) +
                        9 * getNumericValueInPosition(innIp, 7) +
                        4 * getNumericValueInPosition(innIp, 8) +
                        6 * getNumericValueInPosition(innIp, 9) +
                        8 * getNumericValueInPosition(innIp, 10)) % 11) % 10);

        boolean result =
                getNumericValueInPosition(innIp, 10) == getNumericValueInPosition(digit11, 0) &&
                getNumericValueInPosition(innIp, 11) == getNumericValueInPosition(digit12, 0);

        return result;
    }

    private static int getNumericValueInPosition(String taxNumber, int i) {
        return Character.getNumericValue(taxNumber.charAt(i));
    }
}
