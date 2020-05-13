package online.prostobank.clients.domain;

import javax.persistence.*;

/**
 *
 * @author yv
 */
@Entity
@Table(name = "passport_issuers")
public class Issuer {
    @Id
    @GeneratedValue
    private Long id;    
    
    @Basic
    private String code;
    
    @Basic
    private String name;
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    Issuer() {
        
    }
}
