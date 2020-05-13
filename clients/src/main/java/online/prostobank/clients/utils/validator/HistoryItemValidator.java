package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client.HistoryItemDTO;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class HistoryItemValidator {
    private static final String TEXT_LIMIT = "^.{1,1000}$";

    public static Pair<Boolean, List<String>> validate(HistoryItemDTO itemDTO) {
        boolean result = true;
        List<String> messages = new ArrayList<>();

        if (itemDTO == null) {
            messages.add("Данные полностью отсутствуют");
            result = false;
        } else {

            if (itemDTO.getClientId() == null) {
                messages.add("Не указан идентификатор клиента");
                result = false;
            }

            if (itemDTO.getItemType() == null) {
                messages.add("Не указан тип сообщения");
                result = false;
            }

            if (itemDTO.getMessage() == null || !itemDTO.getMessage().trim().matches(TEXT_LIMIT)) {
                messages.add("Сообщение отсутствует или слишком велико");
                result = false;
            }

        }

        return Pair.of(result, messages);
    }
}
