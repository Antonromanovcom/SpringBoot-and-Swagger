package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CompanyKycScoringDTO {
    private Long id;
    private String failedKycScoring;
    private BigDecimal calculatedTotalScore;
}
