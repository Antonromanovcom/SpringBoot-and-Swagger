package online.prostobank.clients.domain;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.enums.ClientType;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/**
 * Информация о клиенте (организации)
 *
 * @author yv
 */
@Slf4j
@MappedSuperclass
public class ClientValueEntity implements Serializable {

	@Basic
	private String name; // наименование клиента (организация) ФИО для ИП

	@Basic
	private String shortName; // короткое название компании (ООО вместо общество с ограниченной ответственностью)

	@Basic
	private String email;

	@NotNull
	@Basic
	private String phone;

	@Basic
	private String inn;

	@Basic
	private String promoInn; //в качестве промокода используется другой ИНН (реферальная программа)
	@Basic
	private String ogrn;

	@Basic
	private String head; //глава организации

	@Basic
	private String address;

	@Basic
	private String kpp;

	@Basic
	private String residentAddress; // адрес фактического проживания

	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String primaryCodes;
	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String secondaryCodes;

	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String blackListedCodes;

	@Basic
	@Type(type = "org.hibernate.type.TextType")
	private String riskyCodes;

	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
	private CompanyKonturFeature konturFeature;

	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
	private CompanyKycScoring companyKycScoring;

	private String headTaxNumber;

	private LocalDate regDate;

	private LocalDate grnRecordDate;

	/**
	 * учредители компании
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "account_application_founders")
	private Set<Founder> founders;

	public ClientValueEntity(@Nonnull String phone) {
		this.phone = phone;
	}

	public ClientValueEntity(@Nonnull String name,
							 @Nonnull String email,
							 @Nonnull String phone,
							 @Nonnull String inn,
							 @Nonnull String ogrn,
							 @Nonnull String head) {
		this.name = StringUtils.trimToEmpty(name); // наименование клиента (организация) ФИО для ИП
		this.email = StringUtils.trimToEmpty(email);
		this.phone = StringUtils.trimToEmpty(phone);
		this.head = StringUtils.trimToEmpty(head); //глава организации
		this.inn = StringUtils.trimToNull(inn);
		this.ogrn = StringUtils.trimToNull(ogrn);

		primaryCodes = secondaryCodes = "";
	}

	protected ClientValueEntity() {

	}

	public String getResidentAddress() {
		return residentAddress;
	}

	public void setResidentAddress(String residentAddress) {
		this.residentAddress = residentAddress;
	}

	public CompanyKonturFeature getKonturFeature() {
		return konturFeature;
	}

	public void setKonturFeature(CompanyKonturFeature konturFeature) {
		this.konturFeature = konturFeature;
	}

	public CompanyKycScoring getCompanyKycScoring() {
		return companyKycScoring;
	}

	public void setCompanyKycScoring(CompanyKycScoring companyKycScoring) {
		this.companyKycScoring = companyKycScoring;
	}


	public void setKpp(String kpp) {
		this.kpp = kpp;
	}

	/**
	 * наименование клиента (организация) ФИО для ИП
	 *
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Короткое наименование компании (ООО вместо общества с ограниченной ответственностью)
	 *
	 * @return
	 */
	public String getShortName() {
		return this.shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getEmail() {
		return this.email == null ? "" : this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * глава организации
	 *
	 * @return
	 */
	public String getHead() {
		return this.head;
	}

	public String getNumber() {
		return StringUtils.isNotBlank(inn) ? StringUtils.trim(inn) : StringUtils.trim(ogrn);
	}

	/**
	 * Получение именно ИНН при вводе на странице заявки. Если не был введён пользователем при регистрации
	 *
	 * @return
	 */
	public String getInn() {
		return inn;
	}

	public String getKpp() {
		return kpp;
	}

	public String getOgrn() {
		return this.ogrn;
	}

	public boolean isSP() {
		return (StringUtils.isNotBlank(ogrn) && ogrn.length() == TaxNumberUtils.IP_OGRN_LENGTH) || (StringUtils.isNotBlank(inn) && inn.length() == TaxNumberUtils.IP_INN_LENGTH);
	}

	public boolean isLlc() {
		return (StringUtils.isNotBlank(ogrn) && ogrn.length() == TaxNumberUtils.OGRN_LENGTH) || (StringUtils.isNotBlank(inn) && inn.length() == TaxNumberUtils.UL_INN_LENGTH);
	}

	public ClientType getClientType(){
		return isSP() ? ClientType.SP : isLlc() ? ClientType.LLC : ClientType.UNKNOWN;
	}

	public String getPromoInn() {
		return promoInn;
	}

	public String getPrimaryCodes() {
		return primaryCodes;
	}

	public String getSecondaryCodes() {
		return secondaryCodes;
	}

	/**
	 * Делает проверку, является ли код явно запрещенным. Перечень запрещенных кодов надо смотреть в APIKUB-183
	 *
	 * @return
	 */
	public boolean hasBlackListedCodes() {
		return StringUtils.isNotBlank(blackListedCodes);
	}

	public String getBlackListedCodes() {
		return blackListedCodes;
	}

	public String getRiskyCodes() {
		return riskyCodes;
	}

	/**
	 * Проверка, есть ли коды этой компании у нас. Для отображения в crm статусов ОКВЭД
	 *
	 * @return
	 */
	public boolean isOkvedCodesEmpty() {
		return blackListedCodes == null && riskyCodes == null;
	}

	/**
	 * Делает проверку, получили ли рискованные коды
	 *
	 * @return
	 */
	public boolean hasRiskyCodes() {
		return StringUtils.isNotBlank(riskyCodes);
	}

	public String getHeadTaxNumber() {
		return headTaxNumber;
	}

	public void setHeadTaxNumber(String headTaxNumber) {
		this.headTaxNumber = headTaxNumber;
	}

	public LocalDate getRegDate() {
		return regDate;
	}

	public void setRegDate(LocalDate regDate) {
		this.regDate = regDate;
	}

	public LocalDate getGrnRecordDate() {
		return grnRecordDate;
	}

	public void setGrnRecordDate(LocalDate grnRecordDate) {
		this.grnRecordDate = grnRecordDate;
	}

	public Set<Founder> getFounders() {
		return founders;
	}

	public void setFounders(Set<Founder> founders) {
		this.founders = founders;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhoneRaw() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPrimaryCodes(String primaryCodes) {
		this.primaryCodes = primaryCodes;
	}


	public void setBlackListedCodes(String blackListedCodes) {
		this.blackListedCodes = blackListedCodes;
	}

	public void setRiskyCodes(String riskyCodes) {
		this.riskyCodes = riskyCodes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public void setInn(String inn) {
		this.inn = inn;
	}

	public void setPromoInnRaw(String promoInn) {
		this.promoInn = promoInn;
	}

	public void setOgrn(String ogrn) {
		this.ogrn = ogrn;
	}

	public void setSecondaryCodes(String secondaryCodes) {
		this.secondaryCodes = secondaryCodes;
	}

	@Override
	public String toString() {
		return "Данные клиента:" +
				"\nНазвание '" + name + '\'' +
				",\nКоорткое название '" + shortName + '\'' +
				",\nEmail='" + email + '\'' +
				",\nТелефон '" + phone + '\'' +
				",\nИНН '" + inn + '\'' +
				",\nПромо-код '" + promoInn + '\'' +
				",\nОГРН '" + ogrn + '\'' +
				",\nРуководитель '" + head + '\'' +
				",\nАдрес '" + address + '\'' +
				",\nКПП '" + kpp + '\'' +
				",\nАдрес регистрации '" + residentAddress + '\'' +
				",\nПервичный ОКВЭД '" + primaryCodes + '\'' +
				",\nВторичные ОКВЭД '" + secondaryCodes + '\'' +
				",\nЧёрные ОКВЭД '" + blackListedCodes + '\'' +
				",\nРискованные ОКВЭД '" + riskyCodes + '\'' +
				(Optional.ofNullable(konturFeature).isPresent() ? ",\nКонтрольные признаки компании " + konturFeature.getFailedFeatures() : "")
				+
				",\nОбщие признаки KYC компании " + companyKycScoring +
				",\nИНН руководителя '" + headTaxNumber + '\'' +
				",\nДата регистрации " + regDate +
				",\nГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения " + grnRecordDate +
				",\nУчредители " + founders +
				'}';
	}
}
