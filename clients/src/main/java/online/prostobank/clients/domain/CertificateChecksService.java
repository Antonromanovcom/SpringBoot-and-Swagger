package online.prostobank.clients.domain;

/**
 * Сервис проверки на выпуск сертификата
 *
 * @author yv
 */
public interface CertificateChecksService {

	/**
	 * Патчим сертификат для "Просто"
	 *
	 */
	void patchCertificate();

	/**
	 * Проверка на наличие сертификата
	 *
	 * @param inn Инн организации
	 * @return True, если сертификат выпущен
	 */
	boolean hasCertificate(String inn);
}
