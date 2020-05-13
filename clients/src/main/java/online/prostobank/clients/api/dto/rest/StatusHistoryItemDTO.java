package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StatusHistoryItemDTO {
    private Long               id;
    private AccountApplication app;
    private String             currentStatus;
    private String             oldStatus;
    private Instant            createdAt;
    private String             eventInitiator;
}
