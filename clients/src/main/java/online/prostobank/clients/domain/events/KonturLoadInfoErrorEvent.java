package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Ошибка получения информации из контура
 */
@Getter
public class KonturLoadInfoErrorEvent extends ApplicationEvent {
    String errorText;

    public KonturLoadInfoErrorEvent(String innOrOgrn, String errorText) {
        super(innOrOgrn);
        this.errorText = errorText;
    }
}