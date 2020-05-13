package online.prostobank.clients.utils.aspects.aj;

import lombok.NoArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Утилитный класс для аспектов
 */
@NoArgsConstructor
final class JoinPointUtils {

    /**
     * Расчитать целевую точку аспекта
     * @param point точка
     * @return цель
     */
    public static Object targetize(final JoinPoint point) {
        Object tgt;
        final Method method = MethodSignature.class
                .cast(point.getSignature()).getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            tgt = method.getDeclaringClass();
        } else {
            tgt = point.getTarget();
        }
        return tgt;
    }
}
