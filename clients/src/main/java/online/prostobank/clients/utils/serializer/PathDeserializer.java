package online.prostobank.clients.utils.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import online.prostobank.clients.domain.attachment.Path;

import java.io.IOException;

/**
 * JSON-сериализатор для обертки {@link Path} (представление пути к узлу хранения в сторадже)
 */
public class PathDeserializer extends StdDeserializer<Path> {

    public PathDeserializer() {
        super(Path.class);
    }

    public PathDeserializer(Class<?> vc) {
        super(vc);
    }

    public PathDeserializer(JavaType valueType) {
        super(valueType);
    }

    public PathDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public Path deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return new Path(node.asText());
    }

}
