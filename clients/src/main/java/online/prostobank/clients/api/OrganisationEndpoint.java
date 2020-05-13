package online.prostobank.clients.api;

import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import online.prostobank.clients.services.KycService;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.constraints.Min;

import static online.prostobank.clients.api.ApiConstants.ORGANIZATION_CONTROLLER;

@Benchmark
@JsonLogger
@RestController
@RequestMapping(
		value = ORGANIZATION_CONTROLLER,
		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
@Transactional
public class OrganisationEndpoint {

	private static final Logger log = LoggerFactory.getLogger(OrganisationEndpoint.class);

	@Autowired
	private KycService kycService;

	@GetMapping(value = "info")
	@ResponseBody
	public ResponseEntity<String> getOrgs(
			@Min(value = 0, message = "Значение должно быть неотрицательным числом") @RequestParam(name = "innOrOgrn") String innOrOgrn
	) {
		log.info("get info about inn {} started", innOrOgrn);

		if (!TaxNumberUtils.isOgrnValid(innOrOgrn) && !TaxNumberUtils.isInnValid(innOrOgrn)) {
			log.warn("Введён невалидный ИНН/ОГРН");
			return new ResponseEntity<>("Введён невалидный ИНН/ОГРН", HttpStatus.OK);
		}

		return new ResponseEntity<>(kycService.getInfoResultRaw(innOrOgrn), HttpStatus.OK);
	}
}
