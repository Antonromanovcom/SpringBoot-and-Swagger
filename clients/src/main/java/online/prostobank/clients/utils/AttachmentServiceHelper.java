package online.prostobank.clients.utils;

import online.prostobank.clients.services.attacment.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Получение бина сервиса взаимодействия с content-storage в методах классов, связанных с UI.
 */
@Service
public class AttachmentServiceHelper {
	private static AttachmentService attachmentService;

	@Autowired
	public void setMyManager(AttachmentService service) {
		attachmentService = service;
	}

	public static AttachmentService getAttachmentService() {
		return attachmentService;
	}
}
