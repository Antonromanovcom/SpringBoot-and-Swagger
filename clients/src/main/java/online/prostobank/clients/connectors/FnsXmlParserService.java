package online.prostobank.clients.connectors;

import club.apibank.connectors.ConnectorFns;
import club.apibank.connectors.ConnectorFnsImpl;
import club.apibank.connectors.helpers.dto.DocumentDto;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.config.properties.FnsXmlParserProperties;
import online.prostobank.clients.connectors.api.IFnsXmlParserService;
import online.prostobank.clients.domain.fns.DocumentFns;
import online.prostobank.clients.domain.fns.InfoEmployeesFns;
import online.prostobank.clients.domain.fns.InfoNpFns;
import online.prostobank.clients.domain.fns.repository.DocumentFnsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FnsXmlParserService implements IFnsXmlParserService {

    private final DocumentFnsRepository documentFnsRepository;
    private final FnsXmlParserProperties config;

    @Override
    public int getInfo(String inn){
        DocumentFns documentFns = documentFnsRepository.findByInnUl(inn);
        return Optional.ofNullable(documentFns)
                .map(DocumentFns::getInfoEmployeesFnsDto)
                .map(InfoEmployeesFns::getCountOfEmployee)
                .orElse(-1);
    }

    @Scheduled(cron = "0 0 */1 ? * *")
    protected void updateInfo(){
        ConnectorFns connectorFns = ConnectorFnsImpl.getInstance(
                config.getBaseUrl(),
                config.getHost(),
                config.getPort(),
                config.getUserName(),
                config.getPass()
        );
        String nameOfLastVersion = connectorFns.nameOfLastVersion();

        boolean fnsCurrent = connectorFns.isCurrent(nameOfLastVersion);
        if(!fnsCurrent){
            List<DocumentDto> documentDtoList = connectorFns.updateFilesOnFtp();
            documentDtoList
                    .forEach(doc -> {
                        DocumentFns documentFns = documentFnsRepository.findByInnUl(doc.getInfoNpDto().getInnUl());
                        if(null != documentFns) {
                            documentFns.setInfoEmployeesFnsDto(
                                    new InfoEmployeesFns(
                                            doc.getInfoEmployeesDto().getCountOfEmployees()
                                    ));
                        } else {
                            documentFns = new DocumentFns(doc.getDateDoc(), doc.getStatusDateDoc(),
                                    new InfoNpFns(doc.getInfoNpDto().getName(),doc.getInfoNpDto().getInnUl()),
                                    new InfoEmployeesFns(doc.getInfoEmployeesDto().getCountOfEmployees()));
                        }
                        documentFnsRepository.save(documentFns);
                    });
        }
    }



}
