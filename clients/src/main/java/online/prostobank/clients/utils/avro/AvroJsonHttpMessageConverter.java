package online.prostobank.clients.utils.avro;

import org.springframework.http.MediaType;

import static online.prostobank.clients.utils.avro.AvroConstants.DEFAULT_CHARSET;

public class AvroJsonHttpMessageConverter<T> extends AvroHttpMessageConverter<T> {
    public AvroJsonHttpMessageConverter() {
        super(false, new MediaType("application", "avro+json", DEFAULT_CHARSET),
                new MediaType("application", "*+avro+json", DEFAULT_CHARSET));

    }
}
