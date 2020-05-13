package online.prostobank.clients.config;

import online.prostobank.clients.utils.CsvHttpMessageConverter;
import online.prostobank.clients.utils.avro.AvroBinaryHttpMessageConverter;
import online.prostobank.clients.utils.avro.AvroJsonHttpMessageConverter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .mediaType(CsvHttpMessageConverter.EXTENSION, CsvHttpMessageConverter.MEDIA_TYPE)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        HttpMessageConverter<?> avroBinary = new AvroBinaryHttpMessageConverter<SpecificRecordBase>();
        HttpMessageConverter<?> avroJson = new AvroJsonHttpMessageConverter<SpecificRecordBase>();
        //Приходится ставить в начало цепочки, иначе у Jackson-конвертера срабатывает application/*+json
        //который пытается конвертить application/avro+json
        converters.add(0, avroBinary);
        converters.add(0, avroJson);
        converters.add(new CsvHttpMessageConverter<>());
    }
}
