package online.prostobank.clients.utils.aspects.aj;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Логирование всех исключений через
 * ${@link ExceptionUtils#getMessage(java.lang.Throwable)}
 */
@Slf4j
@Aspect
@ConditionalOnExpression("${aspect.exception:true}")
@Component
public  class ExceptionLoggerAspect {

    /**
     * Логирование всех исключений в заданном {@link org.aspectj.lang.annotation.Pointcut}
     * todo: Для возмжоности логировать пакеты  более высокого
     *         уровня необходимо изменить структуру пакетов
     * @param point точка
     * @param ex исключение
     */
    @AfterThrowing(
            pointcut = "execution(* online.prostobank.clients.domain.*.*.*(..))",
            throwing = "ex"
    )
    public void logExceptions(JoinPoint point, Exception ex) {
        val exceptionSource = point.getTarget().getClass().toString();
        log.error(
                new StringBuilder()
                        .append("EXCEPTION SOURCE :: \n")
                        .append(exceptionSource)
                        .append("EXCEPTION :: ")
                        .append(ExceptionUtils.getMessage(ex))
                        .toString()
            );
        }
}
