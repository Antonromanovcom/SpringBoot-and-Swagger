package online.prostobank.clients.connectors.ipwhois;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IpWhoisDTO {
    private String ip;
    private String city;
    private String message;
    private boolean success;
}
