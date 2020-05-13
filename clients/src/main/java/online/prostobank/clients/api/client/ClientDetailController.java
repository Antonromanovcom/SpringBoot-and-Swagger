package online.prostobank.clients.api.client;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client_detail.*;
import online.prostobank.clients.services.client.ClientDetailService;
import online.prostobank.clients.services.validation.InboundDtoValidator;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import online.prostobank.clients.utils.validator.*;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static online.prostobank.clients.api.ApiConstants.*;

@Benchmark
@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(CLIENT_CONTROLLER)
public class ClientDetailController {
	private final ClientDetailService clientDetailService;
	private final InboundDtoValidator validator;

	@ApiOperation(value = "Список сотрудников клиента")
	@GetMapping(value = CARD + "/{clientId}/" + EMPLOYEES, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> getAllEmployees(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "clientId") Long clientId) {
		return new ResponseEntity<>(
				clientDetailService.getClientEmployees(clientId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_CARD_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Список бенефициаров клиента")
	@GetMapping(value = CARD + "/{clientId}/" + BENEFICIARIES, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> getAllBeneficiaries(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "clientId") Long clientId) {
		return new ResponseEntity<>(
				clientDetailService.getClientBeneficiaries(clientId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_CARD_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Добавить сотрудника клиенту")
	@PostMapping(value = CARD + "/{clientId}/" + EMPLOYEE, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> createEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "clientId") Long clientId,
													  @RequestBody EmployeeDTO employeeDTO) {
		Pair<Boolean, List<String>>  validation = EmployeeValidator.validate(employeeDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.createEmployee(clientId, employeeDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(CLIENT_CARD_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Добавить бенефициара клиенту")
	@PostMapping(value = CARD + "/{clientId}/" + BENEFICIARY, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> createBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "clientId") Long clientId,
														 @RequestBody BeneficiaryDTO beneficiaryDTO) {
		Pair<Boolean, List<String>>  validation = BeneficiaryValidator.validate(beneficiaryDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.createBeneficiary(clientId, beneficiaryDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(CLIENT_CARD_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Добавить паспорт человеку")
	@PostMapping(value = HUMAN + "/{humanId}/" + PASSPORT, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> addPassportToHuman(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "humanId") Long humanId,
														  @RequestBody HumanPassportDTO passportDTO) {
		Pair<Boolean, List<String>>  validation = PassportValidator.validate(passportDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.addPassportToHuman(humanId, passportDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(HUMAN_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Удалить паспорт")
	@DeleteMapping(value = PASSPORT + "/{passportId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deletePassport(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "passportId") Long passportId) {
		return new ResponseEntity<>(
				clientDetailService.deletePassport(passportId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(PASSPORT_NOT_FOUND),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Добавить телефон сотруднику")
	@PostMapping(value = EMPLOYEE + "/{employeeId}/" + PHONE, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> addPhoneToEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "employeeId") Long employeeId,
														  @RequestBody PhoneDTO phoneDTO) {
		Pair<Boolean, List<String>>  validation = PhoneValidator.validate(phoneDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.addPhoneToEmployee(employeeId, phoneDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(EMPLOYEE_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Удалить телефон у сотрудника")
	@DeleteMapping(value = EMPLOYEE + "/" + PHONE + "/{phoneId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deletePhoneEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "phoneId") Long phoneId) {
		return new ResponseEntity<>(
				clientDetailService.deletePhoneEmployee(phoneId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(PHONE_NOT_FOUND),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Добавить почту сотруднику")
	@PostMapping(value = EMPLOYEE + "/{employeeId}/" + EMAIL, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> addEmailToEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "employeeId") Long employeeId,
														  @RequestBody EmailDTO emailDTO) {
		Pair<Boolean, List<String>>  validation = EmailValidator.validate(emailDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.addEmailToEmployee(employeeId, emailDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(EMPLOYEE_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Удалить почту у сотрудника")
	@DeleteMapping(value = EMPLOYEE + "/" + EMAIL + "/{emailId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deleteEmailEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "emailId") Long emailId) {
		return new ResponseEntity<>(
				clientDetailService.deleteEmailEmployee(emailId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(EMAIL_NOT_FOUND),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Добавить телефон бенефициару")
	@PostMapping(value = BENEFICIARY + "/{beneficiaryId}/" + PHONE, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> addPhoneToBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "beneficiaryId") Long beneficiaryId,
															 @RequestBody PhoneDTO phoneDTO) {
		Pair<Boolean, List<String>>  validation = PhoneValidator.validate(phoneDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.addPhoneToBeneficiary(beneficiaryId, phoneDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(BENEFICIARY_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Удалить телефон у бенефициара")
	@DeleteMapping(value = BENEFICIARY + "/" + PHONE + "/{phoneId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deletePhoneBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "phoneId") Long phoneId) {
		return new ResponseEntity<>(
				clientDetailService.deletePhoneBeneficiary(phoneId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(PHONE_NOT_FOUND),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Добавить почту бенефициару")
	@PostMapping(value = BENEFICIARY + "/{beneficiaryId}/" + EMAIL, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> addEmailToBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "beneficiaryId") Long beneficiaryId,
															 @RequestBody EmailDTO emailDTO) {
		Pair<Boolean, List<String>>  validation = EmailValidator.validate(emailDTO);
		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientDetailService.addEmailToBeneficiary(beneficiaryId, emailDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(BENEFICIARY_NOT_FOUND_OR_NOT_MODIFIED)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Удалить почту у бенефициара")
	@DeleteMapping(value = BENEFICIARY + "/" + EMAIL + "/{emailId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deleteEmailBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "emailId") Long emailId) {
		return new ResponseEntity<>(
				clientDetailService.deleteEmailBeneficiary(emailId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(EMAIL_NOT_FOUND),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Изменить сотрудника (только данные верхнего уровня)")
	@PutMapping(value = EMPLOYEE, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> editEmployee(@NonNull KeycloakAuthenticationToken token, @RequestBody EmployeeDTO employeeDTO) {
		return new ResponseEntity<>(
				clientDetailService.editEmployee(employeeDTO)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(EMPLOYEE_NOT_FOUND_OR_NOT_MODIFIED)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Изменить бенефициара (только данные верхнего уровня)")
	@PutMapping(value = BENEFICIARY, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> editBeneficiary(@NonNull KeycloakAuthenticationToken token, @RequestBody BeneficiaryDTO beneficiaryDTO) {
		return new ResponseEntity<>(
				clientDetailService.editBeneficiary(beneficiaryDTO)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(BENEFICIARY_NOT_FOUND_OR_NOT_MODIFIED)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Изменить сведения о физлице (только данные верхнего уровня)")
	@PutMapping(value = HUMAN, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> editHuman(@NonNull KeycloakAuthenticationToken token, @RequestBody HumanDTO humanDTO) {
		return new ResponseEntity<>(
				clientDetailService.editHuman(humanDTO)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(HUMAN_NOT_FOUND_OR_NOT_MODIFIED)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Удалить бенефициара")
	@DeleteMapping(value = BENEFICIARY + "/{beneficiaryId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deleteBeneficiary(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "beneficiaryId") Long beneficiaryId) {
		return new ResponseEntity<>(
				clientDetailService.deleteBeneficiary(beneficiaryId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(BENEFICIARY_NOT_FOUND_OR_NOT_MODIFIED),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Удалить сотрудника")
	@DeleteMapping(value = EMPLOYEE + "/{employeeId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> deleteEmployee(@NonNull KeycloakAuthenticationToken token, @PathVariable(name = "employeeId") Long employeeId) {
		return new ResponseEntity<>(
				clientDetailService.deleteEmployee(employeeId) ?
						ResponseDTO.goodResponse(ACCEPTED, true) :
						ResponseDTO.badResponse(EMPLOYEE_NOT_FOUND_OR_NOT_MODIFIED),
				HttpStatus.OK);
	}
}
