package online.prostobank.clients.domain.attachment;

import online.prostobank.clients.services.client.ClientAttachmentClass;

/**
 * Функциональные классы документов
 */
public enum DocumentClass {
    USER("user"),
    BANK("bank");

    private String name;

    DocumentClass(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static DocumentClass from(ClientAttachmentClass attachmentClass) {
        switch (attachmentClass) {
            case BANK:
                return DocumentClass.BANK;
            default:
                return DocumentClass.USER;
        }
    }
}
