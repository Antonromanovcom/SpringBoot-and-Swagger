package online.prostobank.clients.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * Информация об учредителях компании
 */
@Getter
@Entity
public class Founder {

	@Id
	@GeneratedValue
	private Long id;

	private String fio;
	private String inn;
	private String ogrn;
	private LocalDate grnRecordDate;

	public Founder() {
	}

	public Founder(String fio, String inn, String ogrn, LocalDate grnRecordDate) {
		this.fio = fio;
		this.inn = inn;
		this.ogrn = ogrn;
		this.grnRecordDate = grnRecordDate;
	}

	@Override
	public String toString() {
		return
				"\nФИО '" + fio + '\'' +
				" - ИНН '" + inn + '\'' ;
	}
}
