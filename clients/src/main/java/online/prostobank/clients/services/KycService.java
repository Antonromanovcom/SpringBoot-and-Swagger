package online.prostobank.clients.services;

import club.apibank.connectors.exceptions.EgrulServiceException;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.KycServiceProperties;
import online.prostobank.clients.connectors.ExternalConnectors;
import online.prostobank.clients.connectors.api.KonturService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Slf4j
public class KycService {

	@Autowired private ExternalConnectors externalConnectors;

	@Autowired
	@Qualifier("ks")
	private KonturService ks;

	@Autowired private KycServiceProperties config;

	public KonturService.InfoResult getInfoResult(@RequestParam(name = "innOrOgrn") String innOrOgrn) {
		KonturService.InfoResult ir = ks.loadInfo(innOrOgrn);
			if (StringUtils.isNotBlank(ir.errorText)) {
				log.warn("При запросе информации из базы ФНС возникла ошибка " + ir.errorText);
// обращение к внешнему сервису отключено, как неприемлемо долгое (отваливаются проверки для анкеты и проч.)
//				try {
//					ir = externalConnectors.getEgripService().getInfo(innOrOgrn);
//				} catch (InterruptedException | EgrulServiceException | IOException e) {
//					log.error("Ошибка при получении чистой информации о компании", e);
//				}
			}
			return ir;
	}

	public String getInfoResultRaw(@RequestParam(name = "innOrOgrn") String innOrOgrn) {
		try {
			return externalConnectors.getEgripService().getInfoRaw(innOrOgrn);
		} catch (InterruptedException | EgrulServiceException | IOException e) {
			log.error("Ошибка при получении чистой информации о компании", e);
			return e.getLocalizedMessage();
		}
	}
}
