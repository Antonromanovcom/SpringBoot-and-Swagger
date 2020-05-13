package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StartWorkDTO {
    private Long               id;
    private Instant            startAt;
    private String             manager;
    private AccountApplication app;
    private String             status;
}
