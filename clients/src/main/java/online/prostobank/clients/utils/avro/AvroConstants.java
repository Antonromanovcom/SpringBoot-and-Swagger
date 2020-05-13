package online.prostobank.clients.utils.avro;

import java.nio.charset.Charset;

public class AvroConstants {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final String AVRO_TYPE = "application";
    public static final String AVRO_SUBTYPE = "avro";
    public static final String AVRO_SUBTYPE_ANY = "*+avro";
}
