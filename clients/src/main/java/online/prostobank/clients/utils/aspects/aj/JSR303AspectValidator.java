package online.prostobank.clients.utils.aspects.aj;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Автоматическая валидация сигнатур методов, результатов, конструкторов
 *{@see <a href="https://jcp.org/en/jsr/detail?id=303">JSR 303</a>}
 */
@Slf4j
@Aspect
@ConditionalOnExpression("${aspect.validation:true}")
@Component
public class JSR303AspectValidator {

    private Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Pointcut("execution(* *(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeMethodPointCut() {}

    @Pointcut("execution(*.new(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeConstructorPointCut() {}

    @Pointcut("execution(@(javax.validation.* || javax.validation.constraints.*) * *(..))")
    public void afterMethodResponsePointCut() {}

    /**
     * Валидация аргументов метода
     * @param point точка выполнения
     */
    @Before("beforeMethodPointCut()")
    public void beforeMethod(JoinPoint point) {
        Optional.ofNullable(this.validator)
                .map(Validator::forExecutables)
                .ifPresent(validator1 -> {
                    this.validateMethod(
                            point.getThis(),
                            ((MethodSignature) point.getSignature()).getMethod(),
                            point.getArgs()
                    );
                });
    }

    /**
     * Валидация аргументов конструктора
     * @param point точка выполнения
     */
    @Before("beforeConstructorPointCut()")
    public void beforeConstructor(JoinPoint point) {
        Optional.ofNullable(validator)
                .map(Validator::forExecutables)
                .map(validator1 -> (Constructor<Object>) ((ConstructorSignature) point.getSignature()).getConstructor())
                .ifPresent(constructor ->
                        validateConstructor(
                                constructor,
                                point.getArgs()
                ));
    }

    /**
     * Валидация результата выполнения
     * @param point точка выполнения
     * @param result результат вызова
     */
    @AfterReturning(
            pointcut = "afterMethodResponsePointCut()",
            returning = "result"
    )
    public void after(JoinPoint point, Object result) {
        this.checkForViolations(
                this.validator
                        .forExecutables()
                        .validateReturnValue(
                                point.getThis(),
                                ((MethodSignature) point.getSignature()).getMethod(),
                                result
                        )
        );
    }

    /**
     * Валидация параметров метода
     * @param object объект в точке выполнения
     * @param method метод в точке выполнения
     * @param args параметры метода
     */
    private void validateMethod(Object object,
                                Method method,
                                Object... args) {
        this.checkForViolations(
                this.validator
                        .forExecutables()
                        .validateParameters(
                                object,
                                method,
                                args
                        )
        );
    }

    /**
     * Валидация параметров конструктора
     * @param ctr конструктор в точке выполнения
     * @param args параметры
     */
    private void validateConstructor(Constructor<Object> ctr,
                                     Object... args) {
        this.checkForViolations(
                this.validator
                        .forExecutables()
                        .validateConstructorParameters(
                                ctr,
                                args
                        )
        );
    }

    /**
     * Проверка наличия нарушений {@link ConstraintViolationException}
     * @param violations нарушения
     */
    private void checkForViolations(Set<ConstraintViolation<Object>> violations) {
        Optional.ofNullable(violations)
                .filter(v -> !v.isEmpty())
                .ifPresent(constraintViolations -> {
                    log.error(
                            ExceptionUtils.getRootCauseMessage(
                                    new ConstraintViolationException(
                                            JSR303AspectValidator.pack(constraintViolations),
                                            violations
                                    )
                            )
                    );
                });
    }

    /**
     * Упаковка сообщений исключений в строку
     * @param errs все нарушения
     * @return все сообщения исключений
     */
    private static String pack(Collection<ConstraintViolation<Object>> errs) {
        return errs.stream()
                //todo: more detail constraint description
                .map(constraint -> constraint.getPropertyPath() + " :: " + constraint.getMessage())
                .filter(o -> !o.isEmpty())
                .collect(Collectors.joining("\n :: "));
    }
}
