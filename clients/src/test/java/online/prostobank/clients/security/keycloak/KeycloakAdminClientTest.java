package online.prostobank.clients.security.keycloak;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.mockito.Mock;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class KeycloakAdminClientTest {

    @Mock private KeycloakAdminClient keycloakAdminClientUnderTest;

    @Before
    public void setUp() {
        initMocks(this);
        keycloakAdminClientUnderTest =  mock(KeycloakAdminClient.class);
    }

    @Test
    public void testFetchUsers() {
        // Setup
        final int first = 0;
        final int last = 0;
        final Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>>  expectedResult = new HashMap<>();

        // Run the test
        final Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>> result = keycloakAdminClientUnderTest.fetchUsers(first, last);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFetchRolesByUsername() {
        // Setup
        final String username = "username";
        final Optional<List<RoleRepresentation>> expectedResult = Optional.empty();

        // Run the test
        final Optional<List<RoleRepresentation>> result = keycloakAdminClientUnderTest.fetchRolesByUsername(username);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetUserByUsername() {
        // Setup
        final String username = "username";
        final List<UserRepresentation> expectedResult = Arrays.asList();

        // Run the test
        final List<UserRepresentation> result = keycloakAdminClientUnderTest.getUserByUsername(username);

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
