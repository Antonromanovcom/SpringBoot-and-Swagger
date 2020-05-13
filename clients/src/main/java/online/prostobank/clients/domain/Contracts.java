package online.prostobank.clients.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter
@Setter
@Embeddable
public class Contracts {
	private boolean produce;
	private boolean lend;
	private boolean subcontract;
	private boolean service;
	private boolean market;
	private boolean loan;
	private boolean actives;
	private boolean license;
	private boolean agent;
	private boolean other;
	private String otherText;
}
