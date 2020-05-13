package online.prostobank.clients.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuestionnaireValue implements Serializable {
	private String businessType;
	private String realCompanySize;
	private String isChiefAccountantPresent;
	private String officialSite;
	private String accountNoSignPermission;
}
