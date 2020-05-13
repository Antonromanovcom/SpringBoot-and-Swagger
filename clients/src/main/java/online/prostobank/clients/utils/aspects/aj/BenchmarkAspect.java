package online.prostobank.clients.utils.aspects.aj;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Подсчет производительности, успешных вызовов,
 * исключений, среднего и общего времени работы
 */
@Slf4j
@Aspect
@ConditionalOnExpression("${aspect.benchmark:true}")
@Component
public class BenchmarkAspect {

    @Autowired @Qualifier("benchmarkCounters") private Set<CallCounter> counterSet;

    @Pointcut("@within(online.prostobank.clients.utils.aspects.Benchmark)")
    public void benchmarkAnnotation() {}

    /**
     * Подсчет количества вызовов, исключений и времени работы
     * @param jp точка выполнения
     */
    @Around("execution(public * *(..)) && benchmarkAnnotation()")
    public Object callCounter(ProceedingJoinPoint jp) throws Throwable {
        val invocationSignature    = jp.getSignature().toString();
        val startTime              = Instant.now().toEpochMilli();
        val result                 = jp.proceed(jp.getArgs()); // необходимо для вычислени времени
        val endTime                = Instant.now().toEpochMilli();

        Optional<CallCounter> counter = counterSet.stream()
                .filter(o -> o.getCallSignature().equals(invocationSignature))
                .findFirst();

        if (!counter.isPresent()) {
            val newCounter = new CallCounter(invocationSignature);
            counterSet.add(newCounter);
            incrementSuccess(startTime, endTime, Optional.of(newCounter));
        } else {
            incrementSuccess(startTime, endTime, counter);
        }
        return result;
    }

    /**
     * Подсчет исключений точки выполнения
     * @param jp точка выполнения
     * @param e исключение
     */
    @AfterThrowing(
            pointcut = "execution(public * *(..)) && benchmarkAnnotation()",
            throwing = "e"
    )
    public void exceptionCounter(JoinPoint jp, Throwable e) {
        val invocationSignature = jp.getSignature().getName();

        log.debug("MethodName to look in counterSet :: {}", invocationSignature);
        Optional<CallCounter> counter = counterSet.stream()
                .filter(o -> o.getCallSignature().equals(invocationSignature))
                .findFirst();
        if (counter.isPresent()) {
            val newCounter = new CallCounter(invocationSignature);
            incrementFailure(Optional.of(newCounter));
        } else {
            incrementFailure(counter);
        }
    }

    private void incrementSuccess(long startTime,
                                  long endTime,
                                  Optional<CallCounter> counter) {
        counter.ifPresent(CallCounter::success);
        counter.ifPresent(o -> {
            o.appendExecutionTime(endTime - startTime);
            o.setAverageExecutionTimeMS(o.getTotalExecutionTimeMS() / (o.getSuccess() + o.getFailure()));
        });
    }

    private void incrementFailure(Optional<CallCounter> counter) {
        counter.ifPresent(CallCounter::failure);
        counter.ifPresent( o -> log.trace("Current amount of failures :: {}", o.getFailure()));
    }

}
