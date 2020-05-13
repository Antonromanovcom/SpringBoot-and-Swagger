package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.StatusHistoryItem;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statistics.ReportColumn;
import online.prostobank.clients.domain.statistics.ReportColumnType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
public class AppFlow {
	@ReportColumn(title = "Номер клиента")
	public String clientId = "";
	@ReportColumn(title = "Источник")
	public String source = "";
	@ReportColumn(title = "Город")
	public String city = "";
	@ReportColumn(title = "ИНН")
	public String inn = "";
	@ReportColumn(title = "Статус")
	public String status = "";
	@ReportColumn(title = "Дата с", type = ReportColumnType.DATE_TIME)
	public Instant from;
	@ReportColumn(title = "Дата по", type = ReportColumnType.DATE_TIME)
	public Instant to;
	@ReportColumn(title = "Создатель")
	public String creator = "";
	@ReportColumn(title = "Менеджер")
	public String manager = "";

	public static List<AppFlow> createFrom(AccountApplication application, String appUrl, List<StatusHistoryItem> statusHistoryItemList) {
		ArrayList<AppFlow> appFlows = new ArrayList<>();

		String clientId = appUrl + application.getId().toString();
		String source = application.getSource() != null ? application.getSource().name() : "";
		String name = application.getCity().getName();
		String inn = application.getClient().getInn();
		String creator = application.getCreator();

		int length = statusHistoryItemList.size();
		for (int i = 0; i < length; i++) {
			StatusHistoryItem statusHistoryItem = statusHistoryItemList.get(i);

			/** Вставил такую проверку, так как в таблице sm_transitions образовалось множество статусов со значение null
			 * которые не мог смапить нижеследующий ClientStates::valueOf и вызывал ошибку...
			 */
			if (Stream.of(ClientStates.values()).anyMatch(e -> e.name().equals(statusHistoryItem.getNewState()))) {

				AppFlow appFlow = new AppFlow()
						.setClientId(clientId)
						.setSource(source)
						.setCity(name)
						.setInn(inn)
						.setFrom(statusHistoryItem.getCreatedAt())
						.setTo((length - i <= 1) ? null : statusHistoryItemList.get(i + 1).getCreatedAt())
						.setCreator(creator)
						.setManager(statusHistoryItem.getCreatedBy());

				Optional.ofNullable(statusHistoryItem.getNewState())
						.map(ClientStates::valueOf)
						.map(ClientStates::getRuName)
						.ifPresent(appFlow::setStatus);

				appFlows.add(appFlow);
			}
		}
		return appFlows;
	}
}
