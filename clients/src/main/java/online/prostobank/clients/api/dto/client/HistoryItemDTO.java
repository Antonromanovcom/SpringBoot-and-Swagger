package online.prostobank.clients.api.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.enums.HistoryItemType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HistoryItemDTO {
    @JsonProperty(value = "client_id")
    Long clientId;
    @JsonProperty(value = "item_type")
    HistoryItemType itemType;
    @JsonProperty(value = "message")
    String message;
}
