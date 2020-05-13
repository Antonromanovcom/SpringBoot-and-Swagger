package online.prostobank.clients.domain.recognition;

import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;

import java.util.Optional;

public final class RecognitionFailDocument implements IRecognizedDocument {
    private static final RecognitionFailDocument instance = new RecognitionFailDocument();

    public static RecognitionFailDocument getInstance() {
        return instance;
    }

    private RecognitionFailDocument() {}

    @Override
    public boolean isRecognized() {
        return false;
    }

    @Override
    public AttachmentFunctionalType getFunctionalType() {
        return AttachmentFunctionalType.UNKNOWN;
    }

    @Override
    public Optional<String> getFieldValue(String key) {
        return Optional.empty();
    }
}
