package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

public class InnCheckResult extends AccountApplicationEvent {
    private String message;
    private boolean isSuccess;
    private boolean is550;
    private boolean isScheduler;

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isIs550() {
        return is550;
    }

    public boolean isScheduler() {
        return isScheduler;
    }

    private InnCheckResult(AccountApplication app, String message, boolean isSuccess, boolean is550, boolean isScheduler) {
        super(app);
        this.message = message;
        this.isSuccess = isSuccess;
        this.is550 = is550;
        this.isScheduler = isScheduler;
    }

    public static InnCheckResult successKyc(AccountApplication app) {
        return new InnCheckResult(app, "", true, false, false);
    }

    public static InnCheckResult failKyc(AccountApplication app, String message) {
        return new InnCheckResult(app, message, false, false, false);
    }

    public static InnCheckResult successKycScheduler(AccountApplication app) {
        return new InnCheckResult(app, "", true, false, true);
    }

    public static InnCheckResult failKycScheduler(AccountApplication app, String message) {
        return new InnCheckResult(app, message, false, false, true);
    }

    public static InnCheckResult success550(AccountApplication app) {
        return new InnCheckResult(app, "", true, true, false);
    }

    public static InnCheckResult fail550(AccountApplication app, String message) {
        return new InnCheckResult(app, message, false, true, false);
    }

    public static InnCheckResult success550Scheduler(AccountApplication app) {
        return new InnCheckResult(app, "", true, true, true);
    }

    public static InnCheckResult fail550Scheduler(AccountApplication app, String message) {
        return new InnCheckResult(app, message, false, true, true);
    }
}