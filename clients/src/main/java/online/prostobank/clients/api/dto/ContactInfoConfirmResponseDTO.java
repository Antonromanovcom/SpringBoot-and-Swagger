package online.prostobank.clients.api.dto;

import java.io.Serializable;

public class ContactInfoConfirmResponseDTO implements Serializable {
    public String userId;
    public boolean ok = false;
    public String smsToken;
}
