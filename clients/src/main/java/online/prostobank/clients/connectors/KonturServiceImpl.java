package online.prostobank.clients.connectors;

import club.apibank.connectors.fns.FnsConnector;
import club.apibank.connectors.fns.config.FnsServiceRepositoryConfig;
import club.apibank.connectors.fns.model.dto.CompanyDTO;
import club.apibank.connectors.kontur.KonturKycAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.KonturServiceProperties;
import online.prostobank.clients.connectors.api.KonturService;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.events.KonturGetScorringErrorEvent;
import online.prostobank.clients.domain.events.KonturLoadInfoErrorEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

import static online.prostobank.clients.utils.Utils.DD_MM_YYYY_FORMATTER;

/**
 *
 * @author yv
 */
@Slf4j
@RequiredArgsConstructor
public class KonturServiceImpl implements KonturService {
	public static final String EMPTY_RESULT_BY_TAX_NUMBER = "База ФНС вернула пустой результат";

	private final ApplicationEventPublisher bus;
	private final KonturServiceProperties config;

    /**
     * Проверка по скорингу из KYC
     * @param aa
     * @return
     */
    @Nonnull
	@Override
    public CheckResult makeChecks(AccountApplication aa) {
	    CheckResult cr = new CheckResult();
	    cr.innOrOgrn = aa.getClient().getNumber();
	    injectConnectionProperties();

	    try {
	        cr.scoring = KonturKycAdapter.getRisk(cr.innOrOgrn);
        } catch (Exception ex) { // Да, ловим все :(
	        // sending sysemail
	        bus.publishEvent(new KonturGetScorringErrorEvent(aa, ex));
            log.error("Ошибка получения скоринга для заявки с инн/огрн = " + cr.innOrOgrn, ex);
	        cr.errorText = "Ошибка получения скоринга. " + ex.getLocalizedMessage();
        }
	    log.info("checks retrieved successfully " + cr.toString());
        return cr;
    }


	/**
     * Получение информации из KYC по компании
     * @param innOrOgrn
     * @return
     */
    @Override
    public InfoResult loadInfo(String innOrOgrn) {
        InfoResult res = new InfoResult();

        try {
	        injectConnectionProperties();

            FnsServiceRepositoryConfig fnsServiceRepositoryConfig = new FnsServiceRepositoryConfig(
            		config.getFnsBaseUrl(),
					config.getFnsBaseUserName(),
					config.getFnsBasePassword()
			);
            FnsConnector fnsConnector = new FnsConnector(fnsServiceRepositoryConfig);
            CompanyDTO company = fnsConnector.getCompany(innOrOgrn);
            if(StringUtils.isBlank(company.getInn())) {
	            log.error("В базе ФНС нет данной компании, вернулся пустой результат по ИНН/ОГРН " + innOrOgrn);
	            res.errorText = EMPTY_RESULT_BY_TAX_NUMBER;
	            bus.publishEvent(new KonturLoadInfoErrorEvent(innOrOgrn, res.errorText));
            } else {
                res.name = company.getOrgName();
                res.shortName = company.getShortOrgName();
                res.headName = company.getClientName();
                res.address = company.getRegPlace();
				res.inn = company.getInn();
				res.ogrn = company.getOgrn();
	            if (company.getRegDate() != null) {
		            res.regDate = DD_MM_YYYY_FORMATTER.format(company.getRegDate());
	            }
	            res.kpp = company.getKpp();
                res.regPlace = company.getRegPlace();
                res.type = company.getType();
                res.primaryCodes = company.getPCodes();
                res.secondaryCodes = company.getCCodes();
	            res.headTaxNumber = company.getHeadTaxNumber();
	            res.founders = company.getFounders();
            }
        } catch (Exception e) { // Да, ловим все :(
	        // sending sysemail
	        bus.publishEvent(new KonturLoadInfoErrorEvent(innOrOgrn, e.getLocalizedMessage()));
	        log.error("Ошибка получения данных по заявке ИНН/ОГРН " + innOrOgrn, e);
            res.errorText = "Ошибка получения данных";
        }
        return res;
    }

    public BigDecimal getAllowedTill() {
		return config.getAllowedTill();
	}

	private void injectConnectionProperties() {
		String base;
		String auth;
		final boolean equals = Objects.equals(config.getIsBigDataKycOn(), "1");
		if (equals) { // первое приближение к переезду с контура
			base = config.getBigDataBaseUrl();
			auth = config.getBigDataAuthUrl();
		} else {
			base = config.getBaseUrl();
			auth = config.getAuthUrl();
		}
		log.info("Calling KYC scoring with big data {}", equals);
		KonturKycAdapter.injectConnectionProperties(
				base,
				auth,
				config.getAuthLogin(),
				config.getAuthPassword(),
				config.getApiKey(),
				"");
	}
}
