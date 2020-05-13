package online.prostobank.clients.api.dto.anketa;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public enum DeveloperPayloadDTO {
    PHONE("phone"),
    PROMO("promo"),
    EMPTY(StringUtils.EMPTY);

    @JsonValue
    private final String name;

    @Override
    public String toString() {
        return name.toLowerCase();
    }
}
