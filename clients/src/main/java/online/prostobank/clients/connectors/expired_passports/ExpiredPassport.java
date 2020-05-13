package online.prostobank.clients.connectors.expired_passports;

public class ExpiredPassport {
    private short series;
    private int number;

    public short getSeries() {
        return series;
    }

    public void setSeries(short series) {
        this.series = series;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}