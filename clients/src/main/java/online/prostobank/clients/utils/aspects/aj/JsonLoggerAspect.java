package online.prostobank.clients.utils.aspects.aj;

import com.google.gson.GsonBuilder;
import lombok.val;
import online.prostobank.clients.utils.UserInfoDto;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getUserInfoDto;

/**
 * Логирование входящих и исходящих данных анотированного класса или метода
 * Сериализация объектов в json {@link GsonBuilder}
 * Логирование данных пользователя через {@link KeycloakSecurityContext}
 */
@Aspect
@ConditionalOnExpression("${aspect.logger:true}")
@Component
public class JsonLoggerAspect {

    /**
     * Точка для обертки всех публичных сигнатур
     */
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

    /**
     * Логирование входных данных всех публичных методов классов с аннотацией ${@link online.prostobank.clients.utils.aspects.JsonLogger}
     * @param point получаемая точка выполнения
     */
    @Before("publicMethod() && @within(online.prostobank.clients.utils.aspects.JsonLogger)")
    public void before(JoinPoint point) {
        logJsonInput(point);
    }


    /**
     * Логирование результата выполнения всех публичных методов классов с аннотацией ${@link JsonLogger}
     * @param joinPoint получаемая точка выполнения
     * @param result результат выполнения
     */
    @AfterReturning(
            pointcut = "publicMethod() && @within(online.prostobank.clients.utils.aspects.JsonLogger)",
            returning = "result"
    )
    public void after(JoinPoint joinPoint, Object result) {
        logJsonOutput(joinPoint, result);
    }

    /**
     * Логирование результата выполнения
     * @param point точка выполнения
     * @param result результат выполнения
     */
    private void logJsonOutput(JoinPoint point, Object result) {
        long start = System.currentTimeMillis();

        val targetUserInfo = Optional.of(getUserInfoDto())
                .map(UserInfoDto::toString)
                .orElse("Unknown");
        val targetMethodName = ((MethodSignature) point.getSignature()).getMethod().getName();
        Class<?> targetClass = point.getTarget().getClass();

        log("Output", result, start, targetUserInfo, targetMethodName, targetClass);
    }

    /** Логирование входных данных
     * @param point точка выполнения
     */
    private void logJsonInput(JoinPoint point) {
        long start = System.currentTimeMillis();
        Object result = point.getArgs();

        val targetUserInfo = Optional.of(getUserInfoDto())
                .map(UserInfoDto::toString)
                .orElse("Unknown");
        val targetMethodName = ((MethodSignature) point.getSignature()).getMethod().getName();
        Class<?> targetClass = point.getTarget().getClass();

        log("Input", result, start, targetUserInfo, targetMethodName, targetClass);
    }

    private void log(@Nonnull String inputOutputPrefix,
                     @Nonnull Object result,
                     long start,
                     @Nonnull String targetUserInfo,
                     @Nonnull String targetMethodName,
                     @Nonnull Class<?> targetClass
    ) {
        LoggerFactory.getLogger(targetClass)
                .info(
                        "\n" +
                                "User :: " + targetUserInfo +
                                "\n" +
                                String.format(
                                        "Execution time :: #%s in %s ms",
                                        targetMethodName,
                                        System.currentTimeMillis() - start) +
                                "\n" +
                                inputOutputPrefix + " data:: " +
                                "\n" +
                                new GsonBuilder()
                                        .setPrettyPrinting()
                                        .create()
                                        .toJson(result)
                );
    }
}
