package online.prostobank.clients.domain.statistics;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReportColumn {
    String title() default StringUtils.EMPTY;
    ReportColumnType type() default ReportColumnType.STRING;
}
