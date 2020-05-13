package online.prostobank.clients.domain.business_service;

public enum DurationTariff {
    YEAR("Год"),
    MONTH("Месяц"),
    THREE_DAYS("3 дня");

    private String name;

    DurationTariff(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
