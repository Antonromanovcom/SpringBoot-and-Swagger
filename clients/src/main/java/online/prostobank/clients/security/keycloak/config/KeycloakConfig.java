package online.prostobank.clients.security.keycloak.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import online.prostobank.clients.security.keycloak.CustomKeycloakAuthenticationProvider;
import online.prostobank.clients.security.keycloak.RoleResolvingGrantedAuthoritiesMapper;
import online.prostobank.clients.security.keycloak.SecurityProperties;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakBaseSpringBootConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/**
 * Конфигурация {@link KeycloakWebSecurityConfigurerAdapter}
 */
@RequiredArgsConstructor
@KeycloakConfiguration
@EnableConfigurationProperties({
        KeycloakSpringBootProperties.class,
        SecurityProperties.class
})
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

    private final RoleHierarchy roleHierarchy;

    @Autowired KeycloakClientRequestFactory requestFactory;

    /**
     * Специальная конфигурация {@link KeycloakWebSecurityConfigurerAdapter}
     * @param http {@link HttpSecurity}
     * @throws Exception исключения
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().disable()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/",
                        "/manifest.webmanifest",
                        "/sw.js",
                        "/offline-page.html",
                        "/v2/api-docs",
                        "/configuration/**",
                        "/api/clients/documents/*",
                        "/swagger*/**",
                        "/webjars/**",
                        "/icons/**",
                        "/images/**",
                        "/frontend/**",
                        "/api/v3/*",
                        "/api/v3/**",
                        "/api/v4/*",
                        "/api/v4/**",
                        "/api/chatbot/*",
                        "/api/chatbot/**",
                        "/api/external/*",
                        "/api/external/**",
                        "/external/**",
                        "/api/tss/*",
                        "/api/tss/**",
                        "/api/clients/**"
                ).permitAll()
                // авторизация
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .permitAll()
                .and()
                .logout()
                .permitAll()
        ;
    }

    /**
     * Получение конфигурации {@link KeycloakConfigResolver} из файла
     * @return {@link KeycloakConfigResolver}
     * на стейдж пощадке возникает колизия с инициализацией бинов,
     * решение {@link CustomKeycloakSpringBootConfigResolver},
     * напрямую инициализировать нельзя
     */
    /*@Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
    */

    /**
     * Специальный {@link KeycloakAuthenticationProvider} который
     * предусматривает добавление {@link RoleHierarchy}
     * @return {@link KeycloakAuthenticationProvider}
     */
    @Override
    protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {

        val grantedAuthorityMapper = new SimpleAuthorityMapper();
//        grantedAuthorityMapper.setConvertToUpperCase(true);
        grantedAuthorityMapper.setPrefix("");

        val resolvingMapper = new RoleResolvingGrantedAuthoritiesMapper(
                roleHierarchy,
                grantedAuthorityMapper
        );

        return new CustomKeycloakAuthenticationProvider(resolvingMapper);
    }

    /**
     * Конфигурация глобального провайдера аунтификаций
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    /**
     * Стратегия регистрации и аунтификации сессии
     * @return {@link SessionAuthenticationStrategy}
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    /**
     * Конфигурация {@link KeycloakRestTemplate} для использования
     * {@link org.keycloak.representations.AccessToken} текущего пользователя
     *
     * @param requestFactory фабрика {@link ClientHttpRequest}  объектов
     *                       создаваемых для сервера для взаимодействия через
     *                       {@code OAuth2 bearer} токен Keycloak
     * @return {@link KeycloakRestTemplate}
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakRestTemplate keycloakRestTemplate(KeycloakClientRequestFactory requestFactory) {
        return new KeycloakRestTemplate(requestFactory);
    }

    /**
     * Проверка регистрации {@link KeycloakSpringBootConfigResolver}
     * при явном отключении автоконфигурации в файле {@code application.yml}
     * {@code keycloak.enabled: false}.
     */
    @Configuration
    static class CustomKeycloakBaseSpringBootConfiguration extends KeycloakBaseSpringBootConfiguration {
//        @Override
//        public void setKeycloakConfigResolvers(KeycloakConfigResolver configResolver) {
//            // устранение рекурсивного вызова setKeycloakConfigResolvers
//        }
    }
}
