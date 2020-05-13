package online.prostobank.clients.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.enums.AttachmentMimeType;
import online.prostobank.clients.services.StorageException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

@Getter
public class Attachment {

	@Setter
	private Long id;

	@Setter
	@NotNull
	private String attachmentName;
	@NotNull
	private Instant createdAt;

	@Setter
	@Getter(onMethod = @__(@JsonIgnore))
	private byte[] content;

	@NotNull
	private AttachmentMimeType attType;

	@NotNull
	private AttachmentFunctionalType functionalType;

	@Setter
	private Boolean quality;

	@Setter
	private String path = "";

	@Setter
	transient private String documentId = "";

	@Setter
	private boolean migrated;

	@Setter
    private Boolean verified;

	@Setter
	private String versionName;

	@Setter
	private String versionCreator;

	@Setter
	private boolean isCurrentVersion;

	public Attachment() {

	}

	/**
	 * Основной способ создания вложения
	 */
	public Attachment(@Nonnull String attachmentName,
					  @Nonnull Instant when,
					  @Nonnull String mimeType) throws StorageException {
		this.attachmentName = attachmentName;
		this.createdAt = when;
		this.attType = AttachmentMimeType.guessFromMime(mimeType);
	}

	public Attachment(String attachmentName, Instant when, String mimeType,
					  AttachmentFunctionalType functionalType)  throws StorageException {
		this(attachmentName, when, mimeType);
		this.functionalType = functionalType;
	}

	public Attachment(Long id, String attachmentName, Instant when, String mimeType,
					  AttachmentFunctionalType functionalType)  throws StorageException {
		this(attachmentName, when, mimeType);
		this.functionalType = functionalType;
		this.id = id;
	}

	public boolean isRecognizable() {
		return this.functionalType != null && this.functionalType.isRecognizable();
	}

	public boolean isMigrated() {
		return migrated;
	}

	public boolean getVerified() {
		return Boolean.TRUE.equals(this.verified);
	}

	public String getVersionName() {
		return StringUtils.isEmpty(versionName) ? "Текущая" : versionName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Attachment that = (Attachment) o;
		return Objects.equals(attachmentName, that.attachmentName) &&
				Objects.equals(createdAt, that.createdAt) &&
				Objects.equals(path, that.path) &&
				attType == that.attType;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(attachmentName, createdAt, attType);
		result = 31 * result + Objects.hash(path);
		return result;
	}
}
