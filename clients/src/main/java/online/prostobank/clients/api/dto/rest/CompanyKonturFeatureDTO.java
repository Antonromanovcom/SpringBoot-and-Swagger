package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyKonturFeatureDTO {
    private Long id;
    private String failedFeatures;
}
