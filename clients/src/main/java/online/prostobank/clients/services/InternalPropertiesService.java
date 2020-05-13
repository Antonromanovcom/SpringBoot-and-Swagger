package online.prostobank.clients.services;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

@Configuration
public class InternalPropertiesService {

    @Resource public Environment env;

    public @Nonnull
    Properties getAllInternalProperties() {
        final Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .forEach(propName -> props.setProperty(propName, env.getProperty(propName)));
        return props;
    }
}
