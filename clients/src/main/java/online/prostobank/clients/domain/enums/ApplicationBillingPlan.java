package online.prostobank.clients.domain.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ApplicationBillingPlan {
    ECONOMY_START("Просто|Экономный старт"),
    WELL("Просто|Хороший"),
    BUSINESS("Просто|Бизнес"),
    EMPTY("")
    ;

    private String caption;

    ApplicationBillingPlan(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static Set<String> getAllCaptions() {
        return Arrays.stream(ApplicationBillingPlan.values())
                .map(ApplicationBillingPlan::getCaption)
                .collect(Collectors.toSet());
    }
}
