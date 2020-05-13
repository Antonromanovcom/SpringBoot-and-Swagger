package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.AccountValue;

import java.util.Collection;

import static online.prostobank.clients.security.UserRolesConstants.NO_ACCOUNT_NUMBER;

@Value
@Builder
public class AccountDTO {
	private String bankBik;
	private String bankInn;
	private String accountNumber;
	private String requestId;

	public static AccountDTO createFrom(AccountApplication application, Collection<String> roles) {
		AccountValue account = application.getAccount();
		AccountDTOBuilder accountDTOBuilder = builder()
				.bankBik(account.getBankBik())
				.bankInn(account.getBankInn());
		if (roles.stream().noneMatch(NO_ACCOUNT_NUMBER::contains)) {
			accountDTOBuilder.accountNumber(account.getAccountNumber());
		}
		accountDTOBuilder.requestId(account.getRequestId());
		return accountDTOBuilder.build();
	}
}
