package online.prostobank.clients.utils.avro;

import org.springframework.http.MediaType;

import static online.prostobank.clients.utils.avro.AvroConstants.*;

public class AvroBinaryHttpMessageConverter<T> extends AvroHttpMessageConverter<T> {

    public AvroBinaryHttpMessageConverter() {
        super(
                true,
                new MediaType(AVRO_TYPE, AVRO_SUBTYPE, DEFAULT_CHARSET),
                new MediaType(AVRO_TYPE, AVRO_SUBTYPE_ANY, DEFAULT_CHARSET)
        );
    }

}
