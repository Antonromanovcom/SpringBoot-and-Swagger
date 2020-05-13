package online.prostobank.clients.api.client;

import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.*;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.services.validation.InboundDtoValidator;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Mock;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.List;

import static online.prostobank.clients.api.dto.client.CheckType.SCORING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClientControllerTest {
	private ClientController clientControllerUnderTest;

	@Mock
	private ClientService mockClientService;

	private InboundDtoValidator validator = new InboundDtoValidator() {
		@Override
		public Pair<Boolean, List<String>> validate(ClientCardCreateDTO dto) {
			return Pair.of(true, Collections.emptyList());
		}

		@Override
		public Pair<Boolean, List<String>> validate(HistoryItemDTO itemDTO) {
			return Pair.of(true, Collections.emptyList());
		}

		@Override
		public Pair<Boolean, List<String>> validate(PassportDTO passportDTO) {
			return Pair.of(true, Collections.emptyList());
		}
	};

	@Mock
	private List<GrantedAuthority> authorities;
	@Mock
	private KeycloakAuthenticationToken token;

	@Before
	public void setUp() {
		initMocks(this);
		clientControllerUnderTest = new ClientController(mockClientService, validator);
		when(token.getAuthorities())
				.thenReturn(authorities);
		when(token.getName())
				.thenReturn("mock");
	}

	@Test
	public void testSaveEditClientInfo() {
		// Setup
		final ClientEditDTO dto = mock(ClientEditDTO.class);
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.saveEditClientInfo(token, dto);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSaveEditQuestionnaire() {
		// Setup
		final QuestionnaireDTO dto = mock(QuestionnaireDTO.class);
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.saveEditQuestionnaire(token, dto);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testGetAll() {
		// Setup
		final ClientGridRequest dto = mock(ClientGridRequest.class);
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.getAll(token, dto);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testFindById() {
		// Setup
		final long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.findById(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testCheckClient() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.checkClient(token, id, SCORING);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSaveComment() {
		// Setup
		final Long id = 0L;
		final String text = "text";
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.saveComment(token, id, text);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSendToUser() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.resendToUser(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSmsReminder() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.smsReminder(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testExportPdf() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<InputStreamResource> result = clientControllerUnderTest.exportPdf(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testAssignToMe() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.assignToMe(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testAssignTo() {
		// Setup
		final Long id = 0L;
		final String newUser = "newUser";
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.assignTo(token, id, newUser);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testStartWork() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.startWork(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSmsCheck() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.smsCheck(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testResetSmsCheck() {
		// Setup
		final Long id = 0L;
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.resetSmsCheck(token, id);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void testSmsConfirmation() {
		// Setup
		final Long id = 0L;
		final String code = "code";
		final ResponseEntity<ResponseDTO> expectedResult = mock(ResponseEntity.class);

		// Run the test
		final ResponseEntity<ResponseDTO> result = clientControllerUnderTest.smsConfirmation(token, id, code);

		// Verify the results
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}
}
