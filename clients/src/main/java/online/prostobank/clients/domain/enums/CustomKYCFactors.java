package online.prostobank.clients.domain.enums;

import java.math.BigDecimal;

public enum CustomKYCFactors {
	FINANCE_RESULT("FinanceResult", "Финансовый результат отрицательный/нулевой", BigDecimal.valueOf(0.1)),
	HEAD_CHANGES_COUNT("HeadChangesCount", "Количество изменений руководителя за последний год больше или равно 1", BigDecimal.valueOf(0.1)),
	FOUNDER_CHANGED("FounderChanged", "С момента смены учредителя прошло менее месяца", BigDecimal.valueOf(0.15)),
	EMPLOYEES_COUNT_ZERO("EmployeesCountZero", "Количество сотрудников = 0", BigDecimal.valueOf(0.15)),
	EMPLOYEES_COUNT_ONE("EmployeesCountOne", "Количество сотрудников = 1", BigDecimal.valueOf(0.1)),
	;

	private String key;
	private String name;
	private BigDecimal weight;

	CustomKYCFactors(String key, String name, BigDecimal weight) {
		this.key = key;
		this.name = name;
		this.weight = weight;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getWeight() {
		return weight;
	}
}
