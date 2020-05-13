package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.statuses.StatusValue;

public class StatusChangedEvent extends AccountApplicationEvent {
    public final StatusValue prev;
    public final StatusValue next;

    public StatusChangedEvent(AccountApplication accountApplication, StatusValue prev, StatusValue next) {
        super(accountApplication);
        this.prev = prev;
        this.next = next;
    }
}