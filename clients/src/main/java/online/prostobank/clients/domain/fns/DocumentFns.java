package online.prostobank.clients.domain.fns;

import javax.persistence.*;
import java.util.Date;
@Entity
@Table(name = "dict_fns")
public class DocumentFns {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Basic
    private Date dateDoc;
    @Basic
    private Date statusDateDoc;
    @Embedded
    private InfoNpFns infoNpFnsDto;
    @Embedded
    private InfoEmployeesFns infoEmployeesFnsDto;

    public DocumentFns(Date dateDoc, Date statusDateDoc, InfoNpFns infoNpFnsDto, InfoEmployeesFns infoEmployeesFnsDto) {
        this.dateDoc = dateDoc;
        this.statusDateDoc = statusDateDoc;
        this.infoNpFnsDto = infoNpFnsDto;
        this.infoEmployeesFnsDto = infoEmployeesFnsDto;
    }

    public DocumentFns() {
    }

    public Long getId() {
        return id;
    }

    public Date getDateDoc() {
        return dateDoc;
    }

    public Date getStatusDateDoc() {
        return statusDateDoc;
    }

    public InfoNpFns getInfoNpFnsDto() {
        return infoNpFnsDto;
    }

    public InfoEmployeesFns getInfoEmployeesFnsDto() {
        return infoEmployeesFnsDto;
    }

    public void setInfoEmployeesFnsDto(InfoEmployeesFns infoEmployeesFnsDto) {
        this.infoEmployeesFnsDto = infoEmployeesFnsDto;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDateDoc(Date dateDoc) {
        this.dateDoc = dateDoc;
    }

    public void setStatusDateDoc(Date statusDateDoc) {
        this.statusDateDoc = statusDateDoc;
    }

    public void setInfoNpFnsDto(InfoNpFns infoNpFnsDto) {
        this.infoNpFnsDto = infoNpFnsDto;
    }
}
