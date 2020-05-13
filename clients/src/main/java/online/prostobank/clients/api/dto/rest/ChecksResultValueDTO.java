package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChecksResultValueDTO {
    private Double konturCheck;
    private String konturErrorText;
    private String p550check;
    private String arrestsFns;
    private String smevCheck;
    private String passportCheck;
    private String p550checkHead;
    private String p550checkFounder;
}
