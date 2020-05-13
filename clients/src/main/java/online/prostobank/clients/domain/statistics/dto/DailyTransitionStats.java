package online.prostobank.clients.domain.statistics.dto;

import java.time.LocalDate;

public class DailyTransitionStats {
	public LocalDate date;
	public String readableStatus;
	public String city;
	public Long from;
	public Long to;
	public Long total;
}
