package online.prostobank.clients.utils.avro;

import org.apache.avro.specific.SpecificRecordBase;

public interface AvroProvider {
	<T extends SpecificRecordBase> T deserialize(byte[] data, Class<T> avroClass);

	<T extends SpecificRecordBase> T deserialize(String data, Class<T> avroClass);

	<T extends SpecificRecordBase> byte[] serialize(T avroObject);
}
