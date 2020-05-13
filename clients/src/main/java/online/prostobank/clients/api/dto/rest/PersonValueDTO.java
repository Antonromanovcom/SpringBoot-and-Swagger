package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PersonValueDTO {
    private String    ser;
    private String    num;
    private String    snils;
    private String    pob;
    private LocalDate dob;
    private String    issuerCode;
    private LocalDate doi;
    private String    issuer;
}
