package online.prostobank.clients.domain.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class Employee {
	private final Long id;
	private final String position;
	private final Human human;
	private final List<Phone> phones;
	private final List<Email> emails;
}
