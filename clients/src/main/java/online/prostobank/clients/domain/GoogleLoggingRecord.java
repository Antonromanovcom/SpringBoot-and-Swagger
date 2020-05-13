package online.prostobank.clients.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Хранит записи обращений и ответов к/от гугл аналитике
 */
@Entity
@DiscriminatorValue("GANALYST")
public class GoogleLoggingRecord extends LoggingRecord {
}
