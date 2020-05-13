package online.prostobank.clients.services.dcc;

import online.prostobank.clients.api.dto.DccDTO;
import online.prostobank.clients.api.dto.DccResponseDTO;
import org.springframework.http.ResponseEntity;

public interface DccService {
	ResponseEntity<DccResponseDTO> createApplication(DccDTO dto);
}
