package online.prostobank.clients.api.dto.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.services.forui.ChecksService;
import org.springframework.data.util.Pair;

import java.util.function.BiFunction;

@Getter
@RequiredArgsConstructor
public enum CheckType {
	SCORING(ChecksService::kontur),
	P_550(ChecksService::recheckP550),
	ARRESTS(ChecksService::arrestsFns),
	PASSPORT(ChecksService::checkPassport),
	;

	private final BiFunction<ChecksService, AccountApplication, Pair<?, AccountApplication>> function;
}
