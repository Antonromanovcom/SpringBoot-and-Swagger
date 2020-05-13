package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 * Событие установки времени звонка клиенту для аналитики
 */
public class CallBackForAnalytics extends  AccountApplicationEvent {
    private boolean isTimeChanged;
    private LocalTime newTime;
    private LocalDate newDate;

    public static CallBackForAnalytics time(AccountApplication app, LocalTime time) {
        CallBackForAnalytics obj = new CallBackForAnalytics(app);
        obj.isTimeChanged = true;
        obj.newTime = time;
        return obj;
    }

    public static CallBackForAnalytics date(AccountApplication app, LocalDate date) {
        CallBackForAnalytics obj = new CallBackForAnalytics(app);
        obj.isTimeChanged = true;
        obj.newDate = date;
        return obj;
    }

    private CallBackForAnalytics(AccountApplication app) {
        super(app);
    }

    public boolean isTimeChanged() {
        return isTimeChanged;
    }

    public String getNewTime() {
        return newTime == null ? "no data" : newTime.toString();
    }

    public String getNewDate() {
        return newDate == null ? "no data" : newDate.toString();
    }
}