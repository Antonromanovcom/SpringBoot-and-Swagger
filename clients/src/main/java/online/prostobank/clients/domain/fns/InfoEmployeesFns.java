package online.prostobank.clients.domain.fns;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class InfoEmployeesFns {
    @Column(name = "count_of_employee")
    @NotNull
    private int countOfEmployee;

    public void setCountOfEmployee(int countOfEmployee) {
        this.countOfEmployee = countOfEmployee;
    }

    public InfoEmployeesFns(int countOfEmployee) {
        this.countOfEmployee = countOfEmployee;
    }

    public int getCountOfEmployee() {
        return countOfEmployee;
    }

    public InfoEmployeesFns() {
    }
}
