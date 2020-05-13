package online.prostobank.clients.utils.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Логирование исключений
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface ExceptionLogger {
}
