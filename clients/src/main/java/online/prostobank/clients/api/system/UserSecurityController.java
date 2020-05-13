package online.prostobank.clients.api.system;


import com.google.gson.GsonBuilder;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import online.prostobank.clients.security.keycloak.PermissionResolver;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.stream.Collectors;

import static online.prostobank.clients.api.ApiConstants.USER_SECURITY_CONTROLLER;

@RequestMapping(USER_SECURITY_CONTROLLER)
@RestController
@RequiredArgsConstructor
class UserSecurityController {

    private final PermissionResolver permissionResolver;

    @ApiOperation(value = "Получение прав текущего пользователя")
    @GetMapping("/current")
    public Object getUserSecurityInfo(@AuthenticationPrincipal KeycloakAuthenticationToken token) {
        val userInfo = new HashMap<>();

        userInfo.put("username", token.getName());
        userInfo.put(
                "roles",
                token.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
        userInfo.put("permissions", permissionResolver.resolve(token));

        return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(userInfo);
    }
}
