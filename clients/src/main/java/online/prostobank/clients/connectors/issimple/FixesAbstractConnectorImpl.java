package online.prostobank.clients.connectors.issimple;

import club.apibank.connectors.isimple.util.FixesService;
import online.prostobank.clients.config.properties.FixesAbstractConnectorImplProperties;
import online.prostobank.clients.connectors.api.IsimpleAbstractConnector;
import online.prostobank.clients.domain.CertificateChecksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Честная реализация для проверки выпусщенной криптобиблиотеки в isimple
 *
 * @author yv
 */
public class FixesAbstractConnectorImpl extends IsimpleAbstractConnector implements CertificateChecksService {

	@Autowired private FixesAbstractConnectorImplProperties config;

	private volatile FixesService fixes;

	public FixesAbstractConnectorImpl() {

	}

	/**
	 * Проверка на наличие сертификата
	 *
	 * @param inn
	 * @return
	 */
	@Override
	public boolean hasCertificate(String inn) {
		Assert.notNull(inn, "`inn` must not bw null");
		init();
		if (config.getProstoCryptoId()> 0) { // проверка на то, что указан валидный идентификатор
			return fixes.hasCertificate(config.getProstoCryptoId(), inn);
		}
		return true;
	}
	
	private synchronized void init() {
		if(fixes == null) {
			fixes = new FixesService(ds(), null, null);
		}
	}

	/**
	 * Патчим сертификат "Просто"
	 */
	@Override
	public void patchCertificate() {
		init();
		if (config.getProstoCryptoId() > 0) { // проверка на то, что указан валидный идентификатор
			fixes.patchCertificates(config.getProstoCryptoId(), config.getProstoGroupName());
		}
	}
}
