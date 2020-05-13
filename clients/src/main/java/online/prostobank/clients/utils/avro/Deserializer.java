package online.prostobank.clients.utils.avro;

public interface Deserializer<T> {

    /**
     * Deserialize object from a byte array.
     * @param clazz the expected class for the deserialized object
     * @param data the byte array
     * @return T object instance
     */
    T deserialize(Class<? extends T> clazz, byte[] data) throws SerializationException;

}

