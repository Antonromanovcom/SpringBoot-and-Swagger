package online.prostobank.clients.utils.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import online.prostobank.clients.domain.attachment.IUniqueId;
import online.prostobank.clients.domain.attachment.UniqueId;

import java.io.IOException;

/**
 * JSON-сериализатор для уникальных идентификаторв типа {@link IUniqueId}
 */
public class UniqueIdDeserializer extends StdDeserializer<IUniqueId> {
    protected UniqueIdDeserializer() {
        super(IUniqueId.class);
    }

    protected UniqueIdDeserializer(Class<?> vc) {
        super(vc);
    }

    protected UniqueIdDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected UniqueIdDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public IUniqueId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return new UniqueId(node.asText());
    }

}
