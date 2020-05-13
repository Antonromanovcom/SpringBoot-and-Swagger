package online.prostobank.clients.domain;

import online.prostobank.clients.domain.enums.BankId;

/**
 *
 * @author yurij
 */
public interface BankInformation {
	
	String getBik(BankId bank);
	String getInn(BankId bank);
	String getName(BankId bank);
}
