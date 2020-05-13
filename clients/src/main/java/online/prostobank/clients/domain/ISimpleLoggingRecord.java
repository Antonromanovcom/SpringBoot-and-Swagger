package online.prostobank.clients.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Хранит записи обращений и ответов к/от емарсиса
 */
@Entity
@DiscriminatorValue("ISIMPLE")
public class ISimpleLoggingRecord extends LoggingRecord {
}