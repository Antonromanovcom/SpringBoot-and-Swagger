package online.prostobank.clients.domain;

import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Хранение результатов проверок и ошибок при проверке заявки об внешние сервисы
 *
 * @author yv
 */
@Embeddable
public class ChecksResultValue {

	public static final String OK = "OK";
	public static final String HAVE_ARREST = "Есть аресты";
	public static final String ERROR = "Ошибка";
	@Basic
	private Double konturCheck;
	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String konturErrorText;
	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String p550check;
	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String arrestsFns;
	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String smevCheck;
	private String passportCheck;
	private String p550checkHead;
	private String p550checkFounder;

	/**
	 * Требуется инфраструктурой JPA
	 */
	ChecksResultValue() {

	}

	public String getSmevCheck() {
		return smevCheck;
	}

	public void setSmevCheck(String smevCheck) {
		this.smevCheck = smevCheck;
	}

	public Double getKonturCheck() {
		return konturCheck;
	}

	public String getKonturErrorText() {
		return konturErrorText;
	}

	public void setKonturCheck(BigDecimal konturCheck) {
		this.konturCheck = konturCheck.doubleValue();
	}

	public String getP550check() {
		return p550check;
	}

	public String getArrestsFns() {
		return arrestsFns;
	}

	public void setArrestsFns(String arrestsFns) {
		this.arrestsFns = arrestsFns;
	}

	public void setP550check(String p550check) {
		this.p550check = p550check;
	}

	public void setKonturErrorText(String errorText) {
		this.konturErrorText = errorText;
	}

	public String getP550checkHead() {
		return p550checkHead;
	}

	public void setP550checkHead(String p550checkHead) {
		this.p550checkHead = p550checkHead;
	}

	public String getP550checkFounder() {
		return p550checkFounder;
	}

	public void setP550checkFounder(String p550checkFounder) {
		this.p550checkFounder = p550checkFounder;
	}

	public String getPassportCheck() {
		return passportCheck;
	}

	public void setPassportCheck(String passportCheck) {
		this.passportCheck = passportCheck;
	}

}
