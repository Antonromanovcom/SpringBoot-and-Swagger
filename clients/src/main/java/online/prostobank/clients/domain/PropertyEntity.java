package online.prostobank.clients.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity(name = "PropertyEntity")
@Table(name = "property_storage")
@DynamicUpdate
@DynamicInsert
public class PropertyEntity {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyEntity.class);

    @Id
    @Basic
    protected String key;

    @Basic
    protected String value;

    @Basic
    protected Instant update;

    public PropertyEntity() {

    }

    public PropertyEntity(
            @Nonnull String key,
            @Nonnull String value
    ) {
        this.key = key;
        this.value = value;
        this.update = Instant.now();
    }

    public @Nonnull String getKey() {
        return key;
    }

    public @Nonnull String getValue() {
        return value;
    }

    public @Nonnull Instant getUpdate() {
        return update;
    }

    public void setKey(@Nonnull String key) {
        this.key = key;
        this.update = Instant.now();
    }

    public void setValue(@Nonnull String value) {
        this.value = value;
        this.update = Instant.now();
    }

    @Override
    public String toString() {
        return "PropertyEntity{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", update=" + update +
                '}';
    }
}
