package online.prostobank.clients.api.dto.rest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.enums.AttachmentMimeType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class AttachmentDTO {
    private String                   id;
    @Setter(AccessLevel.PRIVATE)
    private Long                     clientId;
    private String                   attachmentName;
    private Instant                  createdAt;
    private byte[]                   content;
    private AttachmentMimeType       attType;
    private AttachmentFunctionalType functionalType;
    private Boolean                  quality;
    private String                   path;
    private boolean                  migrated;
    private Boolean                  verified;
    private String                   versionName;
    private String                   versionCreator;
    private List<AttachmentDTO>      versions;

    private AttachmentDTO(String id, String attachmentName, Instant createdAt, AttachmentMimeType attType,
                          AttachmentFunctionalType functionalType, Boolean quality, String path, boolean verified,
                          String versionName, String versionCreator) {
        this.id = id;
        this.attachmentName = attachmentName;
        this.createdAt = createdAt;
        this.attType = attType;
        this.functionalType = functionalType;
        this.quality = quality;
        this.path = path;
        this.verified = verified;
        this.versionName = versionName;
        this.versionCreator = versionCreator;
        this.versions = new ArrayList<>();
    }

    public static AttachmentDTO createFrom(Attachment attachment){
        return new AttachmentDTO(
                String.valueOf(attachment.getId()),
                attachment.getAttachmentName(),
                attachment.getCreatedAt(),
                attachment.getAttType(),
                attachment.getFunctionalType(),
                attachment.getQuality(),
                attachment.getPath(),
                attachment.getVerified(),
                attachment.getVersionName(),
                attachment.getVersionCreator()
        );
    }

    public static AttachmentDTO createFrom(Attachment attachment, Long clientId) {
        AttachmentDTO attachmentDTO = createFrom(attachment);
        attachmentDTO.setClientId(clientId);
        return attachmentDTO;
    }

    public static List<AttachmentDTO> createListFrom(Set<Attachment> attachments, Long clientId) {
        if (attachments == null || attachments.size() == 0) {
            return Collections.emptyList();
        }
        //родительские документы (текущие версии документов)
        List<AttachmentDTO> currentDocuments = attachments.stream()
                .filter(Attachment::isCurrentVersion)
                .map(it -> createFrom(it, clientId))
                .collect(Collectors.toList());

        //клеим к родителям их версии
        currentDocuments.forEach(parent -> parent.versions.addAll(
                attachments.stream()
                        .filter(candidate -> !candidate.isCurrentVersion())
                        .filter(candidate -> isVersion(parent, candidate))
                        .map(it -> createFrom(it, clientId))
                        .collect(Collectors.toList()))
        );
        return currentDocuments;
    }

    public static List<AttachmentDTO> createListFrom(Set<Attachment> attachments) {
        return createListFrom(attachments, null);
    }

    private static boolean isVersion(AttachmentDTO parent, Attachment candidate) {
        return parent.getFunctionalType().isUnique() && parent.getFunctionalType().equals(candidate.getFunctionalType())
                ||
                parent.getAttachmentName().equals(candidate.getAttachmentName()) && parent.getFunctionalType().equals(candidate.getFunctionalType());
    }
}
