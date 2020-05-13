package online.prostobank.clients.utils.aspects;

import com.google.gson.GsonBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Логирование входящих и исходящих данных анотированного класса или метода
 * Сериализация объектов в json {@link GsonBuilder}
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonLogger {
}
