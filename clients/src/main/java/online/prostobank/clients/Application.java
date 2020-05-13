package online.prostobank.clients;

import club.apibank.connectors.analytics.Analytics;
import club.apibank.connectors.analytics.IAnalytics;
import club.apibank.connectors.kontur.RiskDTO;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.ApplicationProperties;
import online.prostobank.clients.config.properties.KonturServiceProperties;
import online.prostobank.clients.connectors.EgripService;
import online.prostobank.clients.connectors.KonturServiceImpl;
import online.prostobank.clients.connectors.RosstatServiceImpl;
import online.prostobank.clients.connectors.api.*;
import online.prostobank.clients.connectors.email.EmarsysEmailConnector;
import online.prostobank.clients.connectors.issimple.FixesAbstractConnectorImpl;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.CertificateChecksService;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.services.KycService;
import online.prostobank.clients.utils.SimpleCORSFilter;
import online.prostobank.clients.utils.aspects.aj.CallCounter;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.annotation.MultipartConfig;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Main application class with some configs
 */
@Slf4j
@SpringBootApplication(
        exclude = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                ThymeleafAutoConfiguration.class,
                JmsAutoConfiguration.class,
                SessionAutoConfiguration.class
        }
)
@EnableTransactionManagement
@EnableIntegration
@MultipartConfig
@EnableConfigurationProperties
@EnableScheduling
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class Application {

    private Set<CallCounter> counterList = new HashSet<>();

    @Autowired private ApplicationProperties config;

    public static void main(String[] args) {

        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET).run();
    }

    @Bean
    @Profile("dev-local")
    public CommandLineRunner loadData(AccountApplicationRepository accountApplicationRepository, CityRepository cityRepository) {
        return (args) -> {

            /**
             * Single items test
             */
			/*
			final String IP_CHECKMEEV_INN = "132808730606";
			final String MARBIOPHARM_INN = "1215001662";
			final String OPTTORGMET_INN = "055001458789";
			final String OAO_HLADOKOMBINAT_INN = "2801002973";
			final String OOO_AVTOGRAD_INN = "4632068000";
			final String IP_MITUSOVA_INN = "773504291213";
			final String OOO_GRUZOPEREVOZKI_INN = "3605005458";
			final String ZAO_DANKO_PROMET_INN = "7703221091";
			final String ZHIRNOVSKOYE_ATP_INN = "6134001825";

			ClientValue client = new ClientValue("", "i.hudyakov@apibank.club", "9096405629", "7716047556", null, "");
			ClientValue client2 = new ClientValue("", "i.hudyakov@apibank.club", "9096405629", ZHIRNOVSKOYE_ATP_INN, null, "");
			ClientValue client3 = new ClientValue("", "i.hudyakov@apibank.club", "9096405629", ZHIRNOVSKOYE_ATP_INN, null, "");

			City msc = null;
			City chlb = null;

			if (cityRepository.findByNameIgnoreCase("Москва").isPresent()) {
				msc = cityRepository.findByNameIgnoreCase("Москва").get();
			} else {
//				msc = cityRepository.save(new City("Москва"));
			}

			if (cityRepository.findByNameIgnoreCase("Челябинск").isPresent()) {
				chlb = cityRepository.findByNameIgnoreCase("Челябинск").get();
			} else {
//				chlb = cityRepository.save(new City("Челябинск"));
			}

			AccountApplication aa1 = new AccountApplication(msc, client,
					Instant.now().atZone(ZoneId.systemDefault()), Status.RESERVING.val(), Source.API_TM,null);
			autowire(aa1);
			autowire(aa1.getAccount());
			accountApplicationRepository.save(aa1);

			AccountApplication aa2 = new AccountApplication(chlb, client2,
					Instant.now().atZone(ZoneId.systemDefault()), Status.CONTACT_INFO_CONFIRMED.val(), Source.API_TM, null);
			autowire(aa2);
			autowire(aa2.getAccount());
			accountApplicationRepository.save(aa2);
			*/

            /**
             * Random bulk test
             */
	        /*
	        cityRepository.save(new City("Москва"));
			ClientValue clientRand;
			AccountApplication aaRand;
			for (int i = 1; i <= 2; i++) {
				clientRand = new ClientValue("",
						RandomStringUtils.randomAlphabetic(5, 13) + "@apibank.club",
						i + "-" + RandomStringUtils.randomNumeric(10),
						RandomStringUtils.randomNumeric(10),
						null, "");

				aaRand = new AccountApplication(cityRepository.findByNameIgnoreCase("Москва").get(), clientRand,
						Instant.now().atZone(ZoneId.systemDefault()), Status.GO_ACTIVE.val(), null, null);

				autowire(aaRand);
				autowire(aaRand.getAccount());
				accountApplicationRepository.saveNew(aaRand);
				Thread.sleep(500);
			}*/
        };
    }

    @Bean
    Filter osiv() {
        return new OpenEntityManagerInViewFilter();
    }

    /**
     * Отправшик sms-уведомлений
     *
     * @param senderClassName Класс отправщика
     * @return
     * @throws Throwable
     */
    @Bean
    public SmsSender sender(@Value("${app.sms-sender-class}") String senderClassName)
            throws Throwable {
        // создаем объект класса по переданному имени класса коннектора
        // предполагаем, что есть конструктор без аргументов
        return (SmsSender) Class.forName(senderClassName).newInstance();
    }

    /**
     * Отправщик почты в smtp (yandex)
     *
     * @param senderClassName
     * @param senderName
     * @return
     * @throws Throwable
     */
    @Bean
    @Qualifier("smtpMailer")
    public EmailConnector emailer(@Value("${app.email-sender-class}") String senderClassName,
                                  @Value("${app.email-sender-name}") String senderName) throws Throwable {
        //создаем объект класса по переданному имени класса коннектора
        // предполагаем, что есть публичный конструктор, принимающий имя отправителя
        Object cc = ReflectionUtils
                .accessibleConstructor(Class.forName(senderClassName), String.class)
                .newInstance(senderName);
        return (EmailConnector) cc;
    }

    /**
     * Отправщик почты в emarsys
     *
     * @param http
     * @return
     */
    @Bean
    @Qualifier("emarsysMailer")
    public EmailConnector emailerEmarsys(@Qualifier("jacksonRestTemplate") RestTemplate http) {
        return new EmarsysEmailConnector(
                http,
                config.getEmarsysUser(),
                config.getEmarsysKey(),
                config.getEmarsysUrl()
        );
    }

    /**
     * Сервис проверок в контуре
     *
     * @param fake Надо ли инстанциировать настоящий
     */
    @Bean
    @Lazy
    public KonturService ks(
            @Value("${app.kontur.fake:false}") boolean fake,
            @Value("${kontur.allowed.till.val}") BigDecimal allowedTill,
            ApplicationEventPublisher bus, KonturServiceProperties config
    ) {
        if (!fake) {
            return new KonturServiceImpl(bus, config);
        }
        return new KonturService() {
            @Nonnull
            @Override
            public CheckResult makeChecks(AccountApplication aa) {
                return new CheckResult() {
                    {
                        RiskDTO scoring = new RiskDTO();
                        scoring.setRisk(BigDecimal.valueOf(0.5));
                        this.scoring = scoring;
                    }
                };
            }

            @Override
            public InfoResult loadInfo(String innOrOgrn) {
                InfoResult ir = new InfoResult() {
                    {
                        headName = "Иванов";
                        inn = innOrOgrn;
                        ogrn = innOrOgrn;
                        name = "ООО Рога и копыта";
                        address = "Ул. Большая Садовая, дом. 12";
                    }
                };
                return ir;
            }

            @Override
            public BigDecimal getAllowedTill() {
                return allowedTill;
            }
        };
    }

    /**
     * Сервис парсинга страницы Росстата
     *
     * @return bean сервиса
     */
    @Bean("rsInn")
    public RosstatService rsInn(){
        return new RosstatServiceImpl();
    }

    @Bean
    @Qualifier("jacksonRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter()));
    }

    @Bean("certs")
    public CertificateChecksService fs() {
        return new FixesAbstractConnectorImpl();
    }

    /**
     * Настройка для фронта. Дать возможность работать с заголовками из js
     *
     * @return
     */
    @Bean
    public SimpleCORSFilter simpleCORSFilter() {
        return new SimpleCORSFilter();
    }

    /**
     * Получение информации о компании из выписки с сайта ЕГРЮЛ
     *
     * @return
     */
    @Bean
    public IEgripService egripService() {
        return new EgripService();
    }

    @Bean
    public KycService kycService() {
        return new KycService();
    }

    @Bean
    public StandardPBEStringEncryptor encryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(config.getSeed());
        return encryptor;
    }

    @Bean(destroyMethod = "shutdown")
    public IAnalytics getAnalytics(@Value("${analytics.production:true}") boolean isProduction) {
        return Analytics.getInstance(isProduction);
    }

    @Bean(name = "benchmarkCounters")
    public Set<CallCounter> counterList() {
        return counterList;
    }
}
