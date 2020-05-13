package online.prostobank.clients.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.Test;

import java.time.LocalDate;

public class LocalDateJsonSerialization {

    @Test
    public void serialization() {
        ObjectMapper mapper = new ObjectMapper();
        DataJson dataJson = new DataJson(LocalDate.now());
        try {
            System.out.println(mapper.writeValueAsString(dataJson));
        } catch (JsonProcessingException ex) {

        }
    }

    @Value
    @AllArgsConstructor
    public static class DataJson {
        private LocalDate testLocalDate;
    }
}
