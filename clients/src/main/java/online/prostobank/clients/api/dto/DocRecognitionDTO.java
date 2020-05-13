package online.prostobank.clients.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocRecognitionDTO {
    @JsonProperty(value = "documentType")
    private String documentType;
    @JsonProperty(value = "stringFields")
    private List<DocFieldDTO> fields;

    public String getDocumentType() {
        return documentType == null ? StringUtils.EMPTY : documentType;
    }

    public List<DocFieldDTO> getFields() {
        return fields == null ? Collections.emptyList() : fields;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocFieldDTO {
        @JsonProperty(value = "name")
        private String name;
        @JsonProperty(value = "utf8value")
        private String value;
        @JsonProperty(value = "accepted")
        private boolean accepted;

        public String getName() {
            return name == null ? StringUtils.EMPTY : name;
        }

        public String getValue() {
            return value == null ? StringUtils.EMPTY : value;
        }

        public boolean isAccepted() {
            return accepted;
        }
    }
}
