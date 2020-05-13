package online.prostobank.clients.domain;

import club.apibank.connectors.kontur.RiskDTO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Хранится инфа по контрольным признакам компании
 */
@Entity
public class CompanyKonturFeature {

	@Id
	@GeneratedValue
	private Long id;

	private String failedFeatures;

	public CompanyKonturFeature() {
	}

	public CompanyKonturFeature(RiskDTO riskDTO) {
		this.failedFeatures = String.join(",", riskDTO.getFailedFeatureList());
	}

	public Long getId() {
		return id;
	}

	public String getFailedFeatures() {
		return failedFeatures;
	}

}
