/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package online.prostobank.clients.connectors.api;

import club.apibank.connectors.fns.model.dto.FounderDto;
import club.apibank.connectors.kontur.RiskDTO;
import lombok.ToString;
import online.prostobank.clients.domain.AccountApplication;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Проверки в "Контур"
 * @author yv
 */
public interface KonturService {
    /**
     * Сделать проверки в Контуре
     * @param aa - заявка
     * @return - результат проверки
     */
    @Nonnull CheckResult makeChecks(@NotNull(message = "Парамерт не задан") AccountApplication aa);

    /**
     * Результат проверки
     * @param innOrOgrn - инн или огрн
     * @return - результат проверки {@link InfoResult}
     */
    InfoResult loadInfo(@NotNull(message = "Парамерт не задан") String innOrOgrn);

    /**
     * Результат проверки по умолчанию
     * @param aa - заявка
     * @return результат проверки {@link InfoResult}
     */
    default InfoResult loadInfo(@NotNull(message = "Парамерт не задан") AccountApplication aa) {
        return loadInfo(aa.getClient().getNumber());
    }

    /**
     * Получить параметр версии
     * @return значени версии
     */
    BigDecimal getAllowedTill();

    class CheckResult {
        public RiskDTO scoring;
        public String errorText;

        public String innOrOgrn;
        @Override
	    public String toString() {
		    return "CheckResult{" +
				    "scoring=" + scoring +
				    ", errorText='" + errorText + '\'' +
				    ", innOrOgrn='" + innOrOgrn + '\'' +
				    '}';
	    }
    }

    /**
     * Результат запроса информации по инн
     */
    @ToString
    class InfoResult {
        public String name;
        public String shortName;
        public String headName;
        public String inn;
        public String kpp;
		public String ogrn;
        public String address;
        public String errorText;
		public String regDate;
		public String regPlace;
        public String type;

        public String[] primaryCodes;
	    public String[] secondaryCodes;
	    public String headTaxNumber;
	    public List<FounderDto> founders;
		public String grnRecord;
    }
}
