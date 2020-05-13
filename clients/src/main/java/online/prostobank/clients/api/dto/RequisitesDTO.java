package online.prostobank.clients.api.dto;

import lombok.Data;

/**
 *
 * @author yv
 */
@Data
public class RequisitesDTO {
    private String requisites;
    private Bank bank = new Bank();
    
    public static class Bank {
        public String name = "«КУБ» (АО)";
        public String bik = "047516949";
        public String inn = "7414006722";
        public String correspondentAccount = "30101810700000000949";
        public String correspondentAddress = "РКЦ г.Магнитогорск Отделения по Челябинской области Уральского главного управления Центрального банка Российской Федерации";
        public String kpp = "997950001";
        public String ogrn = "1027400000638";
    }
}
