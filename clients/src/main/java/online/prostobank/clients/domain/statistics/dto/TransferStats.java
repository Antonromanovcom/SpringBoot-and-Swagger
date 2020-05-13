package online.prostobank.clients.domain.statistics.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.HistoryItem;
import online.prostobank.clients.domain.StartWork;
import online.prostobank.clients.domain.StatusHistoryItem;
import online.prostobank.clients.domain.repository.status_log.StatusHistoryRepository;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statistics.ReportColumn;
import online.prostobank.clients.domain.statistics.ReportColumnType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static online.prostobank.clients.services.forui.AccountApplicationViewServiceImpl.PREFIX_TEXT_ASSIGNED_TO;

@Data
@Accessors(chain = true)
public class TransferStats {
	@ReportColumn(title = "Идентификатор")
	public String id;
	@ReportColumn(title = "Менеджер")
	public String manager;
	@ReportColumn(title = "Источник")
	public String source;
	@ReportColumn(title = "UTM")
	public String utm;
	@ReportColumn(title = "Телефон")
	public String phone;
	@ReportColumn(title = "Организация")
	public String name;
	@ReportColumn(title = "ИНН")
	public String inn;

	@ReportColumn(title = "Назначена менеджеру", type = ReportColumnType.DATE_TIME)
	public Instant assignedToManagerDate;
	@ReportColumn(title = "Текущий статус")
	public String status;
	@ReportColumn(title = "Дата создания завки", type = ReportColumnType.DATE_TIME)
	public Instant dateCreated;
	@ReportColumn(title = "Начать работу", type = ReportColumnType.DATE_TIME)
	public Instant startWork;
	@ReportColumn(title = "Время работы менеджера")
	public String workTime;
	@ReportColumn(title = "Последняя активность", type = ReportColumnType.DATE_TIME)
	public Instant lastActivityDate;

	@ReportColumn(title = "Создан", type = ReportColumnType.DATE_TIME)
	public Instant newClient;
	@ReportColumn(title = "Информация подтверждена", type = ReportColumnType.DATE_TIME)
	public Instant contactInfoConfirmed;
	@ReportColumn(title = "Недозвон", type = ReportColumnType.DATE_TIME)
	public Instant noAnswer;
	@ReportColumn(title = "Проверка лида", type = ReportColumnType.DATE_TIME)
	public Instant checkLead;
	@ReportColumn(title = "Отказ клиента", type = ReportColumnType.DATE_TIME)
	public Instant clientDeclined;
	@ReportColumn(title = "Ожидание документов", type = ReportColumnType.DATE_TIME)
	public Instant waitForDocs;
	@ReportColumn(title = "Документы вложены", type = ReportColumnType.DATE_TIME)
	public Instant documentsExists;
	@ReportColumn(title = "Дозапрос документов", type = ReportColumnType.DATE_TIME)
	public Instant requiredDocs;
	@ReportColumn(title = "В процессе открытия счёта", type = ReportColumnType.DATE_TIME)
	public Instant managerProcessing;
	@ReportColumn(title = "Счет открыт", type = ReportColumnType.DATE_TIME)
	public Instant activeClient;
	@ReportColumn(title = "Счет закрыт", type = ReportColumnType.DATE_TIME)
	public Instant inactiveClient;
	@ReportColumn(title = "Автоматический отказ", type = ReportColumnType.DATE_TIME)
	public Instant autoDeclined;

	public static List<TransferStats> transferReport(List<AccountApplication> applications, StatusHistoryRepository statusHistoryRepository) {
		ArrayList<TransferStats> result = new ArrayList<>();

		applications.forEach(application -> {
			String id = application.getId().toString();
			String source = application.getSource() != null ? application.getSource().name() : "";
			String utm = application.getUtm() != null ? application.getUtm().getUtmSource() : "";
			String phone = application.getClient().getPhone();
			String name = application.getClient().getName();
			String inn = application.getClient().getInn();
			String status = application.getClientState() != null ? application.getClientState().getRuName() : "";
			Instant dateCreated = application.getDateCreated();
			Set<HistoryItem> historyItems = application.getItems();
			Set<StartWork> startWorkSet = application.getStartWorkSet();
			List<StatusHistoryItem> statusHistoryItems = statusHistoryRepository.selectAllByClientIdOrdered(application.getId());

			List<String> managers = statusHistoryItems.stream()
					.map(StatusHistoryItem::getCreatedBy)
					.distinct()
					.sorted()
					.collect(Collectors.toList());

			managers.forEach(manager -> {
				Instant lastActivityDate = getLastActivityDate(historyItems, statusHistoryItems, manager);
				Instant startWork = getStartWork(startWorkSet, manager);
				String workTime = getWorkTime(lastActivityDate, startWork);
				Instant assignedToManagerDate = getDateAssignedTo(historyItems, manager);

				// время перехода в статусы клиента
				Instant newClient = null;
				Instant contactInfoConfirmed = null;
				Instant noAnswer = null;
				Instant checkLead = null;
				Instant clientDeclined = null;
				Instant waitForDocs = null;
				Instant documentsExists = null;
				Instant requiredDocs = null;
				Instant managerProcessing = null;
				Instant activeClient = null;
				Instant inactiveClient = null;
				Instant autoDeclined = null;

				for (StatusHistoryItem statusHistoryItem : statusHistoryItems) {
					if (!statusHistoryItem.getCreatedBy().equals(manager)) {
						continue;
					}

					String newState = statusHistoryItem.getNewState();
					Instant createdAt = statusHistoryItem.getCreatedAt();
					if (newState.equals(ClientStates.NEW_CLIENT.name())) {
						newClient = createdAt;
					} else if (newState.equals(ClientStates.CONTACT_INFO_CONFIRMED.name())) {
						contactInfoConfirmed = createdAt;
					} else if (newState.equals(ClientStates.NO_ANSWER.name())) {
						noAnswer = createdAt;
					} else if (newState.equals(ClientStates.CHECK_LEAD.name())) {
						checkLead = createdAt;
					} else if (newState.equals(ClientStates.CLIENT_DECLINED.name())) {
						clientDeclined = createdAt;
					} else if (newState.equals(ClientStates.WAIT_FOR_DOCS.name())) {
						waitForDocs = createdAt;
					} else if (newState.equals(ClientStates.DOCUMENTS_EXISTS.name())) {
						documentsExists = createdAt;
					} else if (newState.equals(ClientStates.REQUIRED_DOCS.name())) {
						requiredDocs = createdAt;
					} else if (newState.equals(ClientStates.MANAGER_PROCESSING.name())) {
						managerProcessing = createdAt;
					} else if (newState.equals(ClientStates.ACTIVE_CLIENT.name())) {
						activeClient = createdAt;
					} else if (newState.equals(ClientStates.INACTIVE_CLIENT.name())) {
						inactiveClient = createdAt;
					} else if (newState.equals(ClientStates.AUTO_DECLINED.name())) {
						autoDeclined = createdAt;
					}
				}

				result.add(new TransferStats()
						.setId(id)
						.setManager(manager)
						.setSource(source)
						.setUtm(utm)
						.setPhone(phone)
						.setName(name)
						.setInn(inn)
						.setAssignedToManagerDate(assignedToManagerDate)
						.setStatus(status)
						.setDateCreated(dateCreated)
						.setStartWork(startWork)
						.setWorkTime(workTime)
						.setLastActivityDate(lastActivityDate)

						.setNewClient(newClient)
						.setContactInfoConfirmed(contactInfoConfirmed)
						.setNoAnswer(noAnswer)
						.setCheckLead(checkLead)
						.setClientDeclined(clientDeclined)
						.setWaitForDocs(waitForDocs)
						.setDocumentsExists(documentsExists)
						.setRequiredDocs(requiredDocs)
						.setManagerProcessing(managerProcessing)
						.setActiveClient(activeClient)
						.setInactiveClient(inactiveClient)
						.setAutoDeclined(autoDeclined)
				);
			});

		});

		return result;
	}

	private static Instant getDateAssignedTo(Set<HistoryItem> historyItems, String manager) {
		return historyItems.stream()
				.filter(historyItem -> {
					String text = historyItem.getText();
					return text != null && text.startsWith(PREFIX_TEXT_ASSIGNED_TO) && text.endsWith(manager);
				})
				.max(Comparator.comparing(HistoryItem::getCreatedAt))
				.map(HistoryItem::getCreatedAt)
				.orElse(null);
	}

	private static Instant getStartWork(Set<StartWork> startWorkSet, String manager) {
		return startWorkSet.stream()
				.filter(e -> e.getManager().equals(manager))
				.findFirst()
				.map(StartWork::getStartAt)
				.orElse(null);
	}

	private static String getWorkTime(Instant lastActivityDate, Instant startWork) {
		Duration between = Duration.between(
				Optional.ofNullable(startWork)
						.orElse(Instant.now()),
				Optional.ofNullable(lastActivityDate)
						.orElse(Instant.now())
		);

		return between.isNegative() ? ""
				: DurationFormatUtils.formatDuration(between.toMillis(), "HH:mm:ss", true);
	}

	private static Instant getLastActivityDate(Set<HistoryItem> historyItems, List<StatusHistoryItem> statusHistoryItems, String manager) {
		Instant lastStatusHistoryItemInstant = statusHistoryItems.stream()
				.filter(item -> item.getCreatedBy().equals(manager))
				.max(Comparator.comparing(StatusHistoryItem::getCreatedAt))
				.map(StatusHistoryItem::getCreatedAt)
				.orElse(null);

		Instant lastHistoryItemInstant = historyItems.stream()
				.filter(historyItem -> manager.equals(historyItem.getEventInitiator()))
				.max(Comparator.comparing(HistoryItem::getCreatedAt))
				.map(HistoryItem::getCreatedAt)
				.orElse(null);

		Instant lastActivityDate;
		if (lastStatusHistoryItemInstant == null && lastHistoryItemInstant == null) {
			lastActivityDate = null;
		} else if (ObjectUtils.compare(lastStatusHistoryItemInstant, lastHistoryItemInstant) > 0) {
			lastActivityDate = lastStatusHistoryItemInstant;
		} else {
			lastActivityDate = lastHistoryItemInstant;
		}
		return lastActivityDate;
	}
}
