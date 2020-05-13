package online.prostobank.clients.api.dto;

public class FieldValidationResult {

    private FieldValidationResult(String name,
                                  boolean isValid,
                                  String error){
        this.name = name;
        this.isValid = isValid;
        this.error = error;
    }

    private String name;
    private boolean isValid;
    private String error;

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getError() {
        return error;
    }

    public static FieldValidationResult ok(String name) {
        return new FieldValidationResult(name, true, null);
    }

    public static FieldValidationResult error(String name, String message){
        return new FieldValidationResult(name, false, message);
    }
}
