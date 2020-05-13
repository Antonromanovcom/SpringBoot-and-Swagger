package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;


/**
 * Деактивация заявки, если уже есть с таким ИНН
 */
@Getter
public class DeactivateClientEvent extends AccountApplicationEvent {
    private String others;

    public DeactivateClientEvent(AccountApplication app, String others) {
        super(app);
        this.others = others;
    }
}
