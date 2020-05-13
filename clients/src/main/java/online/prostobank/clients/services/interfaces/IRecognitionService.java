package online.prostobank.clients.services.interfaces;

import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;

/**
 * Сервис распознавания отсканированных документов
 */
public interface IRecognitionService {
    IRecognizedDocument getDocument(byte[] file, AttachmentFunctionalType functionalType);
}
