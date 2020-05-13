package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.EmailMessagesEntity;
import online.prostobank.clients.domain.messages.MessageToClient;

import java.util.Collection;
import java.util.List;

@Value
@Builder
public final class ClientCardDTO {
	private long clientId;

	private ClientInfoDTO clientInfo;
	private ApplicationInfoDTO applicationInfo;
	private QuestionnaireDTO questionnaire;
	private ChecksDTO checks;
	private ChecksDTOExtended checksExtended;
	private PassportDTO passport;
	private AccountDTO accountInfo;
	private DocumentsDTO documentsInfo;
	private MessagesDTO messages;

	public static ClientCardDTO createFrom(AccountApplication application,
										   DuplicateDTO duplicate,
										   Collection<String> roles,
										   List<EmailMessagesEntity> messagesFromClients,
										   List<MessageToClient> messagesToClients) {
		ClientInfoDTO clientInfo = ClientInfoDTO.createFrom(application, duplicate);
		ApplicationInfoDTO applicationInfoDTO = ApplicationInfoDTO.createFrom(application);
		QuestionnaireDTO questionnaireDTO = QuestionnaireDTO.createFrom(application);
		ChecksDTO checksDTO = ChecksDTO.createFrom(application);
		ChecksDTOExtended checksDTOExtended = ChecksDTOExtended.createFrom(application);
		PassportDTO passportDTO = PassportDTO.createFrom(application);
		AccountDTO accountDTO = AccountDTO.createFrom(application, roles);
		DocumentsDTO documentsDTO = DocumentsDTO.createFrom(application);
		MessagesDTO messagesDTO = MessagesDTO.createFrom(application, messagesFromClients, messagesToClients);
		return builder()
				.clientId(application.getId())
				.clientInfo(clientInfo)
				.applicationInfo(applicationInfoDTO)
				.questionnaire(questionnaireDTO)
				.checks(checksDTO)
				.checksExtended(checksDTOExtended)
				.passport(passportDTO)
				.accountInfo(accountDTO)
				.documentsInfo(documentsDTO)
				.messages(messagesDTO)
				.build();
	}

	public static ClientCardDTO createFrom(AccountApplication application) {
		ClientInfoDTO clientInfo = ClientInfoDTO.createFrom(application, null);
		ChecksDTOExtended checksDTOExtended = ChecksDTOExtended.createFrom(application);
		ChecksDTO checksDTO = ChecksDTO.createFrom(application);
		DocumentsDTO documentsDTO = DocumentsDTO.createFrom(application);
		MessagesDTO messagesDTO = MessagesDTO.createFrom(application, null, null);
		return builder()
				.clientId(application.getId())
				.clientInfo(clientInfo)
				.checks(checksDTO)
				.checksExtended(checksDTOExtended)
				.documentsInfo(documentsDTO)
				.messages(messagesDTO)
				.build();
	}
}
