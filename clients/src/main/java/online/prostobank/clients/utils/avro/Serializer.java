package online.prostobank.clients.utils.avro;

public interface Serializer<T> {

    /**
     * Serialize object as byte array.
     * @param data the object to serialize
     * @return byte[]
     */
    byte[] serialize(T data) throws SerializationException;

}
