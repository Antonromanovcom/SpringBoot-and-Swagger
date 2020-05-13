package online.prostobank.clients.domain.tss;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class TssMessage {
	private String message;
	private String displayUrl;
	private String displayText;
	private String callbackUrl;
	private Object userPassportMeta;
	private boolean needConfirm;
	private ConfirmType confirmType;
	private String phone; // 10 цифр
}
