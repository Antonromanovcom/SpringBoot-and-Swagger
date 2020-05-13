package online.prostobank.clients.utils;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

import static com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS;
import static com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS;

public class CsvHttpMessageConverter<T, L extends List<T>>
        extends AbstractHttpMessageConverter<L> {

    //TODO: работает с очень ограниченным множеством входных данных, надо как-то расширить

    public static MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.defaultCharset());
    public static String EXTENSION = "csv";


    public CsvHttpMessageConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected L readInternal(Class<? extends L> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new RuntimeException("not implemented");
    }

    private ObjectWriter getCsvWriter(L objects) {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = buildSchema(objects)
                .withHeader()
                .withColumnSeparator(';');

        csvMapper.findAndRegisterModules();
        return csvMapper
                .writerFor(List.class)
                .with(schema)
                .with(ALWAYS_QUOTE_STRINGS)
                .with(ALWAYS_QUOTE_EMPTY_STRINGS);
    }

    private CsvSchema buildSchema(List ts) {
        if (ts.isEmpty())
            return CsvSchema.emptySchema();
        else {
            Object t = ts.get(0);
            CsvSchema.Builder builder = CsvSchema.builder();
            for(Field f : t.getClass().getFields()) {
                builder = builder.addColumn(f.getName());
            }
            return builder.build();
        }
    }

    @Override
    protected void writeInternal(L object, HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException {
        try {
            ObjectWriter objectWriter = getCsvWriter(object);
            try (PrintWriter outputWriter = new PrintWriter(outputMessage.getBody())) {
//              UTF-8 BOM, без него эксель не понимает кодировку
                outputWriter.write('\ufeff');
                outputWriter.write(objectWriter.writeValueAsString(object));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}