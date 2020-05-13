package online.prostobank.clients.domain.recognition.interfaces;

import online.prostobank.clients.domain.enums.AttachmentFunctionalType;

import java.util.Optional;

/**
 * Результат распознавания отсканированного документа.
 */
public interface IRecognizedDocument {

    /**
     * Скан документа был успешно распознан
     * @return
     */
    boolean isRecognized();

    /**
     * Тип распознанного документа
     * @return
     */
    AttachmentFunctionalType getFunctionalType();

    /**
     * Значение поля в распознанном документе
     * @param key
     * @return
     */
    Optional<String> getFieldValue(String key);
}
