package online.prostobank.clients.security.keycloak;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.security.RoleServiceProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Служебный клиент для работы с {@link Keycloak}
 * и кэширование его списка пользователей
 */
@Slf4j
@Service
public class KeycloakAdminClient {

    private final RoleServiceProperties    config;
    private final Keycloak                 keycloak;
    private final Supplier<String>         realm     = this::getRealm;
    private final ScheduledExecutorService executor  = Executors.newSingleThreadScheduledExecutor();

    private Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>> usersCache = new HashMap<>();


    public KeycloakAdminClient(@Nonnull RoleServiceProperties config) {
        this.config = config;

        keycloak = KeycloakBuilder.builder()
                .serverUrl(config.getKeyCloakServerUrl())
                .realm(config.getKeyCloakRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(config.getLogin())
                .clientSecret(config.getSecret())
                .build();

        executor.scheduleAtFixedRate(
                () -> this.usersCache = fetchUsers(),
                0,
                1,
                TimeUnit.MINUTES
        );
    }

    @Nonnull
    Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>> fetchUsers(int first, int last) {
        return keycloak
                .realm(realm.get())
                .users()
                .list(first, last)
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                userRepresentation -> fetchAttributesByUuid(userRepresentation.getId()),
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        )
                );
    }

    private Pair<List<RoleRepresentation>, List<UserSessionRepresentation>> fetchAttributesByUuid(@Nonnull String uuid) {
        UserResource userResource = keycloak.realm(realm.get())
                .users()
                .get(uuid);
        List<RoleRepresentation> roleRepresentations = Optional.ofNullable(
                userResource
                        .roles()
                        .getAll()
                        .getRealmMappings()
        )
                .orElseGet(Collections::emptyList);
        List<UserSessionRepresentation> userSessions = Optional.ofNullable(
                userResource.getUserSessions()
        )
                .orElseGet(Collections::emptyList);

        return Pair.of(roleRepresentations, userSessions);
    }

    public Optional<List<RoleRepresentation>> fetchRolesByUsername(@Nonnull String username) {
        return keycloak.realm(realm.get())
                .users()
                .search(username)
                .stream()
                .findFirst()
                .map(userRepresentation ->
                        keycloak.realm(realm.get())
                                .users()
                                .get(userRepresentation.getId())
                                .roles()
                                .getAll()
                                .getRealmMappings()
                );
    }

    public @Nonnull List<UserRepresentation> getUserByUsername(@Nonnull String username) {
        return keycloak
                .realm(realm.get())
                .users()
                .search(username);
    }

    private @Nonnull String getRealm() {
        return config.getKeyCloakRealm();
    }

    private @Nonnull Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>> fetchUsers() {
        val users = keycloak.realm(realm.get()).users();
        val count = users.count();
        log.info("Getting :: {} user representations", count);
        return fetchUsers(0, count);
    }

    public void addUserRoles(String keycloakId, Set<String> roles) {
        // todo from usersCache
        UserResource userResource = keycloak.realm(realm.get())
                .users()
                .get(keycloakId);
        RolesResource rolesResource = keycloak.realm(realm.get()).roles();

        // todo 403?
        List<RoleRepresentation> collect = roles.stream()
                .map(name -> rolesResource.get(name).toRepresentation())
                .collect(Collectors.toList());

        userResource
                .roles()
                .realmLevel()
                .add(collect);
    }

    public void deleteUserRoles(String keycloakId, Set<String> roles) {
        // todo from usersCache
        UserResource userResource = keycloak.realm(realm.get())
                .users()
                .get(keycloakId);

        List<RoleRepresentation> realmMappings = userResource
                .roles().getAll()
                .getRealmMappings();
        List<RoleRepresentation> toDelete = realmMappings.stream()
                .filter(roleRepresentation -> roles.contains(roleRepresentation.getName()))
                .collect(Collectors.toList());

        userResource
                .roles()
                .realmLevel()
                .remove(toDelete);
    }

    List<String> usersByRole(String roleName) {
        return getUsersCache().entrySet().stream()
                .filter(entry -> entry.getValue().getLeft().stream()
                        .anyMatch(roleRepresentation -> roleRepresentation.getName().equalsIgnoreCase(roleName))
                )
                .map(entry -> entry.getKey().getUsername())
                .collect(Collectors.toList());
    }

    public List<UserSessionRepresentation> sessionsById(String uuid) {
        return getUsersCache().entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey().getId(), uuid))
                .findFirst()
                .map(userRepresentationPairEntry -> userRepresentationPairEntry.getValue().getRight())
                .orElseGet(Collections::emptyList);
    }

    public List<RoleRepresentation> rolesById(@Nonnull String uuid) {
        return getUsersCache().entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey().getId(), uuid))
                .findFirst()
                .map(userRepresentationPairEntry -> userRepresentationPairEntry.getValue().getLeft())
                .orElseGet(Collections::emptyList);
    }

    public Map<UserRepresentation, Pair<List<RoleRepresentation>, List<UserSessionRepresentation>>> getUsersCache() {
        if (MapUtils.isEmpty(this.usersCache)) {
            this.usersCache = fetchUsers();
        }
        return this.usersCache;
    }
}
