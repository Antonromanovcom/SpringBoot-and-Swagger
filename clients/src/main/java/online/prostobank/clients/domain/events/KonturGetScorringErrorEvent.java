package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;


/**
 * Ошибка получения скоринга для заявки с инн/огрн
 */
@Getter
public class KonturGetScorringErrorEvent extends AccountApplicationEvent {
    Exception exception;

    public KonturGetScorringErrorEvent(AccountApplication accountApplication, Exception e) {
        super(accountApplication);
        this.exception = e;
    }
}