package online.prostobank.clients.utils.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class SchemaBuilderHelper {


    public static String getModule2SchemaString() {
        return SchemaBuilder.record("UserDto")
                .namespace("online.prostobank.clients.api.dto.module_push")
                .fields()
                .name("clientAccountNumber")
                .type().stringType()
                .noDefault()
                .optionalString("firstName")
                .optionalString("lastName")
                .optionalString("middleName")
                .optionalString("partner")
                .optionalString("timeZone")
                .optionalString("deliveryFrom")
                .optionalString("deliveryTo")
                .endRecord()
                .toString(true);
    }


}
