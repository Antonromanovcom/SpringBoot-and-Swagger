package online.prostobank.clients.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class EmailMessagesEntity {
    private Instant date;
    private String msgContent;
    private long accountApplicationId;
}
