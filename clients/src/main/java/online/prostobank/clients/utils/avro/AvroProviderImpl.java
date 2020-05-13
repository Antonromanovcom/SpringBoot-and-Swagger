package online.prostobank.clients.utils.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Service
public class AvroProviderImpl implements AvroProvider {
	@Override
	public <T extends SpecificRecordBase> T deserialize(byte[] data, Class<T> avroClass) {
		return deserialize(new String(data), avroClass);
	}

	@Override
	public <T extends SpecificRecordBase> T deserialize(String data, Class<T> avroClass) {
		DatumReader<T> reader = new SpecificDatumReader<>(avroClass);
		try {
			Method getClassSchemaMethod = avroClass.getMethod("getClassSchema");
			Decoder decoder = DecoderFactory.get().jsonDecoder(
					(Schema) getClassSchemaMethod.invoke(null), data);
			return reader.read(null, decoder);
		} catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SpecificRecordBase> byte[] serialize(T avroObject) {
		Class<T> avroClass = (Class<T>) avroObject.getClass();
		DatumWriter<T> writer = new SpecificDatumWriter<>(avroClass);
		try {
			Method getClassSchemaMethod = avroClass.getMethod("getClassSchema");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Encoder encoder = EncoderFactory.get().jsonEncoder(
					(Schema) getClassSchemaMethod.invoke(null), out);

			writer.write(avroObject, encoder);
			encoder.flush();
			out.flush();
			return out.toByteArray();
		} catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
