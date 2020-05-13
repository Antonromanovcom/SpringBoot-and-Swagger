package online.prostobank.clients.api.dto;

import java.util.HashMap;
import java.util.Map;

public class CreateApplicationResponseDTO {

    public CreateApplicationResponseDTO() {
        attributes = new HashMap<>();
    }

    public Map<String, FieldValidationResult> attributes;

    public boolean isValid;
    public String message;
}
