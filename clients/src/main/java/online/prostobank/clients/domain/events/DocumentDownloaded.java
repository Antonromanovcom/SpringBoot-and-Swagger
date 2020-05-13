package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;


/**
 * Загрузка документов менеджером или клиентом из редактора завки или ЛК соотв.
 */
@Getter
public class DocumentDownloaded extends AccountApplicationEvent {
    private boolean isDownloadedByManager;
    private AttachmentFunctionalType type;

    public DocumentDownloaded(AccountApplication app, boolean isDownloadedByManager, AttachmentFunctionalType type) {
        super(app);
        this.isDownloadedByManager = isDownloadedByManager;
        this.type = type;
    }
}
