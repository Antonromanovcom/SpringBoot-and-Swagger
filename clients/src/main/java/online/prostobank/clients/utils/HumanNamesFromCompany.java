package online.prostobank.clients.utils;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class HumanNamesFromCompany {

    /**
     * Набор Ф.И.О. директора или ИП по параметрам предприятия
     * @param companyName - полное наименование компании
     * @param headName - ФИО генерального директора
     * @param isLegalEntity - признак юридического лица (false, если ИП или СЗН)
     * @return
     */
    public static Names createNames(String companyName, String headName, boolean isLegalEntity) {
        return new Names(companyName, headName, isLegalEntity);
    }

    public static class Names {
        private String firstName;
        private String middleName;
        private String lastName;

        Names(String companyName, String head, boolean isLegalEntity) {
            boolean isHeadBlank = StringUtils.isBlank(head);
            boolean isCompanyNameBlank = StringUtils.isBlank(companyName);
            if (isLegalEntity) {
                this.firstName = isHeadBlank ? "" :  getSpecificWordFromFIO(head, 1);
                this.middleName = isHeadBlank ? "" : getSpecificWordFromFIO(head, 2);
                this.lastName = isHeadBlank ? "" : getSpecificWordFromFIO(head, 0);
            } else {
                this.firstName = isCompanyNameBlank ? "" : getSpecificWordFromFIO(companyName, 2);
                this.middleName = isCompanyNameBlank ? "" : getSpecificWordFromFIO(companyName, 3);
                this.lastName = isCompanyNameBlank ? "" : getSpecificWordFromFIO(companyName, 1);
            }
        }

        /**
         * Получить имя директора/ИП
         * @return
         */
        public String firstName() {
            return firstName;
        }

        /**
         * Получить отчество директора/ИП
         * @return
         */
        public String middleName() {
            return middleName;
        }

        /**
         * Получить фамилию директора/ИП
         * @return
         */
        public String lastName() {
            return lastName;
        }

        private String getSpecificWordFromFIO(@Nonnull String fio, int num) {
            if (StringUtils.isBlank(fio)) {
                return "";
            }
            String[] separatedFio = fio.split(" ");

            return separatedFio[Math.min(num, separatedFio.length - 1)];
        }
    }
}
