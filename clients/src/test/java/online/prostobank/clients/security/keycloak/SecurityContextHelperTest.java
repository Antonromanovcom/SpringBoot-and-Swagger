package online.prostobank.clients.security.keycloak;

import online.prostobank.clients.security.UserRoles;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SecurityContextHelperTest {

    @Test
    public void testGetCurrentUserPermissions() {
        // Setup
        final Set<String> expectedResult = new HashSet<>();

        // Run the test
        final Set<String> result = SecurityContextHelper.getCurrentUserPermissions();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetCurrentUsername() {
        // Setup
        final String expectedResult = "Unknown";

        // Run the test
        final String result = SecurityContextHelper.getCurrentUsername();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIsCurrentUserGrantedBy() {
        // Setup
        final UserRoles roles = UserRoles.POS_FRONT_HOME;

        // Run the test
        final boolean result = SecurityContextHelper.isCurrentUserGrantedBy(roles.getRoleName());

        // Verify the results
        assertFalse(result);
    }

    @Test
    public void testIsCurrentUserGrantedByAnyOf() {
        // Setup
        final String[] roles = Arrays.stream(UserRoles.values()).map(UserRoles::getRoleName).toArray(String[]::new);

        // Run the test
        final boolean result = SecurityContextHelper.isCurrentUserGrantedByAnyOf(roles);

        // Verify the results
        assertFalse(result);
    }
}
