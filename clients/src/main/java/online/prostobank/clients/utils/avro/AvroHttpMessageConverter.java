package online.prostobank.clients.utils.avro;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

@Slf4j
public abstract class AvroHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {

    private Serializer<SpecificRecordBase> serializer;
    private Deserializer<SpecificRecordBase> deserializer;

    public AvroHttpMessageConverter(boolean useBinaryEncoding, MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
        serializer = new AvroSerializer<>(useBinaryEncoding);
        deserializer = new AvroDeserializer<>(useBinaryEncoding);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return SpecificRecordBase.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        T result = null;
        byte[] data = IOUtils.toByteArray(inputMessage.getBody());
        if (data.length > 0) {
            result = (T) deserializer.deserialize((Class<? extends SpecificRecordBase>) clazz, data);
        }
        return result;
    }

    @Override
    protected void writeInternal(T t, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        byte[] data = serializer.serialize((SpecificRecordBase) t);
        outputMessage.getBody().write(data);
    }

}
