package online.prostobank.clients.domain.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Beneficiary {
	private final Long id;
	private final Human human;
	private final List<Phone> phones;
	private final List<Email> emails;
	private final Integer stakePercent;
	private final Long stakeAbsolute; //в копейках
}
