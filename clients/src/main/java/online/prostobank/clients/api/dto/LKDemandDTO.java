package online.prostobank.clients.api.dto;

/**
 *
 * @author yv
 */
public class LKDemandDTO {
    public static class CompanyDTO {
        public String type;
    }
    
    public static class DemandDTO {
        public String requisites;
    }
    
    public CompanyDTO company = new CompanyDTO();
    
    public DemandDTO demand = new DemandDTO();
}
