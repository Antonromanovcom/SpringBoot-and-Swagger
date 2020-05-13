package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.services.forui.ExcelReportType;
import online.prostobank.clients.services.forui.ExcelService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getRoles;
import static online.prostobank.clients.services.client.ClientAttachmentUtils.encode;
import static online.prostobank.clients.services.client.ClientAttachmentUtils.getStreamResponse;
import static online.prostobank.clients.utils.Utils.DD_MM_YYYY_FORMATTER;

@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/excel/")
public class ExcelController {
	private final ExcelService excelService;

	@ApiOperation(value = "Отчетность")
	@GetMapping(value = "reports")
	public ResponseEntity<InputStreamResource> createWorkbook(@NonNull KeycloakAuthenticationToken token,
															  @RequestParam @NonNull ExcelReportType type,
															  @RequestParam @NonNull Long from,
															  @RequestParam @NonNull Long to) {
		if (isNotAllowed(token, type)) {
			return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
		}

		try {
			Instant fromI = Instant.ofEpochMilli(from);
			Instant toI = Instant.ofEpochMilli(to);
			return excelService.createWorkbook(type, fromI, toI)
					.map(bytes -> getStreamResponse(
							bytes,
							encode(type.getRuName() + "_с_" + DD_MM_YYYY_FORMATTER.format(fromI) + "_по_" + DD_MM_YYYY_FORMATTER.format(toI) + ".xlsx"),
							true))
					.orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private static boolean isNotAllowed(KeycloakAuthenticationToken token, ExcelReportType type) {
		return Optional.ofNullable(type.getRoles())
				.map(roles -> Collections.disjoint(roles, getRoles(token)))
				.orElse(true);
	}
}
