package online.prostobank.clients.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nonnull;

@Data
@AllArgsConstructor
public class ResponseDTO {
    @Nonnull private String message;
    @Nonnull private Result result;
    @Nonnull private Object data;

    public static ResponseDTO badResponse(@Nonnull final String message) {
        return new ResponseDTO(message, Result.ERROR, Result.ERROR.name());
    }

    public static ResponseDTO goodResponse(@Nonnull final String message,
                                           @Nonnull Object data) {
        return new ResponseDTO(message, Result.OK, data);
    }
}
