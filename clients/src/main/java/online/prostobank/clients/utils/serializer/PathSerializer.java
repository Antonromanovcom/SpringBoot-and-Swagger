package online.prostobank.clients.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import online.prostobank.clients.domain.attachment.Path;

import java.io.IOException;

public class PathSerializer extends StdSerializer<Path> {
	public PathSerializer() {
		this(null);
	}
	public PathSerializer(Class<Path> t) {
		super(t);
	}

	@Override
	public void serialize(Path path, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(path.value());
	}
}
