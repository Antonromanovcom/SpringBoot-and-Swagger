package online.prostobank.clients.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Хранит записи обращений и ответов к/от кубу
 */
@Entity
@DiscriminatorValue("KUB")
public class KubLoggingRecord extends LoggingRecord {
}
