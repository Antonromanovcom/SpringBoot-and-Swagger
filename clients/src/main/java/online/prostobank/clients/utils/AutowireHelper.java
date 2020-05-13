package online.prostobank.clients.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AutowireHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private AutowireHelper() {
    }

    
    public static <T> T autowire(T entity) {
        AutowireHelper.applicationContext.getAutowireCapableBeanFactory().autowireBean(entity);        
        return entity;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        AutowireHelper.applicationContext = applicationContext;
    }
}