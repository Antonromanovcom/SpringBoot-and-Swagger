package online.prostobank.clients.domain.fns;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class InfoNpFns {

    @Column(name = "name_doc")
    private String name;
    @Column(name = "inn_ul")
    @NotNull
    private String innUl;

    public InfoNpFns(String name, String innUl) {
        this.name = name;
        this.innUl = innUl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInnUl(String innUl) {
        this.innUl = innUl;
    }

    public String getName() {
        return name;
    }

    public String getInnUl() {
        return innUl;
    }

    public InfoNpFns() {
    }
}
