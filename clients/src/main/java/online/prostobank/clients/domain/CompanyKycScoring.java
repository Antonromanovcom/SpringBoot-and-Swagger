package online.prostobank.clients.domain;

import club.apibank.connectors.kontur.RiskDTO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Хранится информация по общим признакам KYC компании
 */
@Entity
public class CompanyKycScoring {

	@Id
	@GeneratedValue
	private Long id;

	private String failedKycScoring;
	private BigDecimal calculatedTotalScore;

	public CompanyKycScoring() {
	}

	public CompanyKycScoring(RiskDTO riskDTO) {
		this.failedKycScoring = String.join(",", riskDTO.getFailedScoringList());
		this.calculatedTotalScore = riskDTO.getCalculatedTotalScore();
	}

	public Long getId() {
		return id;
	}

	public String getFailedKycScoring() {
		return failedKycScoring;
	}

	public BigDecimal getCalculatedTotalScore() {
		return calculatedTotalScore;
	}


	public void setCalculatedTotalScore(BigDecimal calculatedTotalScore) {
		this.calculatedTotalScore = calculatedTotalScore;
	}

	public void setFailedKycScoring(String failedKycScoring) {
		this.failedKycScoring = failedKycScoring;
	}

	@Override
	public String toString() {
		return "признаки '" + failedKycScoring + '\'' +
				"\nобщий балл " + calculatedTotalScore ;
	}
}
