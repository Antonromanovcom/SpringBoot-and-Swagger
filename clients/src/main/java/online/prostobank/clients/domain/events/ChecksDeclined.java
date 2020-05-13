package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.FailReason;

/**
 * Возникла проблема - отказ по проверкам
 */
@Getter
public class ChecksDeclined extends AccountApplicationEvent {
	public final FailReason reason;

	public ChecksDeclined(AccountApplication aa, FailReason reason) {
		super(aa);
		this.reason = reason;
	}
}

