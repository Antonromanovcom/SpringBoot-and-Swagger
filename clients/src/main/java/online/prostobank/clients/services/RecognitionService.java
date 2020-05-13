package online.prostobank.clients.services;

import club.apibank.connectors.smartengines.model.AttachmentType;
import club.apibank.connectors.smartengines.service.Recognition;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.prostobank.clients.api.dto.DocRecognitionDTO;
import online.prostobank.clients.config.properties.RecognitionServiceProperties;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.recognition.RecognitionFailDocument;
import online.prostobank.clients.domain.recognition.RecognizedDocument;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;
import online.prostobank.clients.domain.statistics.StatisticsRepository;
import online.prostobank.clients.services.interfaces.IRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RecognitionService implements IRecognitionService {
    private Recognition recognition;
    private StatisticsRepository statisticsRepository;

    @Autowired private RecognitionServiceProperties config;

    @PostConstruct
    private void init() {
        recognition = new Recognition(config.isTrialMode());
    }

    @Autowired
    public RecognitionService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public IRecognizedDocument getDocument(byte[] file, AttachmentFunctionalType functionalType) {
        AttachmentType recognizeType = getRecognizeTypeByFunctional(functionalType);
        if (recognizeType == null) {
            return RecognitionFailDocument.getInstance();
        }
        String jsonString;
        ObjectMapper mapper = new ObjectMapper();
        try {
            statisticsRepository.writeSmartEngineUsage(functionalType);
            jsonString = recognition.createRecognition2(recognizeType, file);
            DocRecognitionDTO dto = mapper.readValue(jsonString, DocRecognitionDTO.class);
            return new RecognizedDocument(dto);
        } catch (Exception ex) {
            return RecognitionFailDocument.getInstance();
        }
    }

    private AttachmentType getRecognizeTypeByFunctional(AttachmentFunctionalType functionalType) {
        switch (functionalType) {
            case PASSPORT_FIRST:
                return  AttachmentType.PASSPORT_SCAN;
            case SNILS:
                return AttachmentType.SNILS_SCAN;
            case INN:
                return AttachmentType.INN_SCAN;
            default:
                return null;
        }
    }
}
