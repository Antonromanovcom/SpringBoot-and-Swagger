package online.prostobank.clients.api;

import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class KeycloakControllerTest {

    @Mock private KeycloakAdminClient mockAdminClient;

    private KeycloakController keycloakControllerUnderTest;

    @Before
    public void setUp() {
        initMocks(this);
        keycloakControllerUnderTest = new KeycloakController(mockAdminClient);
    }

    @Test
    public void testGetUsers() {
        // Setup
        final ResponseEntity<ResponseDTO> expectedResult = null;

        // Run the test
        final ResponseEntity<ResponseDTO> result = keycloakControllerUnderTest.getUsers();

        // Verify the results
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testGetUserBy() {
        // Setup
        final String username = "username";
        final ResponseEntity<ResponseDTO> expectedResult = null;

        // Run the test
        final ResponseEntity<ResponseDTO> result = keycloakControllerUnderTest.getUserBy(username);

        // Verify the results
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
