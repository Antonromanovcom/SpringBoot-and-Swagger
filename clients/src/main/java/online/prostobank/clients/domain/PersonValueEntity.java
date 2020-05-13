package online.prostobank.clients.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.PassportDTO;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDate;

@Slf4j
@Data
@MappedSuperclass
public class PersonValueEntity implements Cloneable, Serializable {
	@Nullable
	private String ser;
	@Nullable
	private String num;
	@Nullable
	private LocalDate dob;
	@Nullable
	private String pob;
	@Nullable
	private String issuerCode;
	@Nullable
	private LocalDate doi;
	@Nullable
	private String issuer;
	@Nullable
	private String snils;

	protected PersonValueEntity() {
	}

    public PersonValueEntity (PassportDTO passportDTO) {
        this.ser = passportDTO.getSeries();
        this.num = passportDTO.getNumber();
        this.dob = passportDTO.getDateOfBirth();
        this.pob = passportDTO.getPlaceOfBirth();
        this.issuerCode = passportDTO.getIssuerCode();
        this.doi = passportDTO.getDateOfIssue();
        this.issuer = passportDTO.getIssuer();
        this.snils = passportDTO.getSnils();
    }
}
