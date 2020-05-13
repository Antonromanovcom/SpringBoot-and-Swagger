package online.prostobank.clients.api.dto;

public class PassportInfoDTO {
    public String birthday;
    public String divisionCode;
    public String issueBy;
    public String issueDate;
    public String passport;
    public String placeOfBirth;

    @Override
    public String toString() {
        return "PassportInfoDTO{" +
                "birthday='" + birthday + '\'' +
                ", divisionCode='" + divisionCode + '\'' +
                ", issueBy='" + issueBy + '\'' +
                ", issueDate='" + issueDate + '\'' +
                ", passport='" + passport + '\'' +
                ", placeOfBirth='" + placeOfBirth + '\'' +
                '}';
    }
}
