package online.prostobank.clients.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContractsDTO {
    private boolean produce;
    private boolean lend;
    private boolean subcontract;
    private boolean service;
    private boolean market;
    private boolean loan;
    private boolean actives;
    private boolean license;
    private boolean agent;
    private boolean other;
    private String otherText;
}
