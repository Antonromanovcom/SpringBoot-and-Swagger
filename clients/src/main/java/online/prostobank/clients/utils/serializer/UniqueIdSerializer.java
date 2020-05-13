package online.prostobank.clients.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import online.prostobank.clients.domain.attachment.IUniqueId;

import java.io.IOException;

public class UniqueIdSerializer extends StdSerializer<IUniqueId> {
	public UniqueIdSerializer() {
		this(null);
	}
	public UniqueIdSerializer(Class<IUniqueId> t) {
		super(t);
	}

	@Override
	public void serialize(IUniqueId id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(id.toString());
	}
}
