package online.prostobank.clients.domain.statuses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KycSystemForDeclineCode {
	KONTUR_KYC(1),
	P550(2),
	OKVED(3),
	KONTUR_SCORING(4);

	private final int kycCode;

	public static KycSystemForDeclineCode getByKycCode(Integer kycCode) {
		for (KycSystemForDeclineCode value : values()) {
			if (value.getKycCode() == kycCode) {
				return value;
			}
		}
		return null;
	}
}
