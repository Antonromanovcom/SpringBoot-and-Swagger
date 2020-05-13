package online.prostobank.clients.connectors;

import club.apibank.connectors.InnNalogRuConnector;
import club.apibank.connectors.analytics.IAnalytics;
import lombok.Getter;
import online.prostobank.clients.connectors.api.*;
import online.prostobank.clients.connectors.issimple.IsimpleUserRegistrationReportAbstractConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Getter
@Service
public class ExternalConnectors {

    @Autowired @Qualifier("rsInn")         private RosstatService rosstatInnService;
    @Autowired @Qualifier("emarsysMailer") private EmailConnector emarsysMailer;
    @Autowired @Qualifier("smtpMailer")    private EmailConnector smtpMailer;

    @Autowired private SmsSender            smsSender;
    @Autowired private IArrestsCheckService checksService;
    @Autowired private IEgripService        egripService;
    @Autowired private AbsService           absService;
    @Autowired private IFnsXmlParserService fnsXmlParserService;
    @Autowired private CityByIpDetector     cityByIpDetector;
    @Autowired private IsimpleUserRegistrationReportAbstractConnector isimpleUserRegistrationReportAbstractConnector;

    // пакетные зависимости
    @Autowired private InnNalogRuConnector innNalogRuConnector;
    @Autowired private IAnalytics          iAnalytics;
}
