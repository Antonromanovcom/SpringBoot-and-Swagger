package online.prostobank.clients.domain.recognition;

import online.prostobank.clients.api.dto.DocRecognitionDTO;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecognizedDocument implements IRecognizedDocument {

    private AttachmentFunctionalType type;
    private Map<String, String> values = new HashMap<>();

    public RecognizedDocument(DocRecognitionDTO dto) {
        for (DocRecognitionDTO.DocFieldDTO fieldDTO : dto.getFields()) {
            values.put(fieldDTO.getName(), fieldDTO.getValue());
        }
        this.type = getFunctionalType(dto.getDocumentType());
    }

    @Override
    public boolean isRecognized() {
        return this.type != AttachmentFunctionalType.UNKNOWN && !values.isEmpty();
    }

    @Override
    public AttachmentFunctionalType getFunctionalType() {
        return type;
    }

    @Override
    public Optional<String> getFieldValue(String key) {
        return Optional.ofNullable(values.get(key));
    }

    private AttachmentFunctionalType getFunctionalType(String dtoType) {
        if (dtoType.contains("rus.passport")) {
            return AttachmentFunctionalType.PASSPORT_FIRST;
        } else if (dtoType.contains("rus.inn")) {
            return AttachmentFunctionalType.INN;
        } else if (dtoType.contains("rus.snils")) {
            return AttachmentFunctionalType.SNILS;
        }
        return AttachmentFunctionalType.UNKNOWN;
    }
}
