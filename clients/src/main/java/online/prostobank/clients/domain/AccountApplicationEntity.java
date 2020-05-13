package online.prostobank.clients.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.enums.ApplicationBillingPlan;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.LastModifiedDate;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class AccountApplicationEntity {
	@Id
	@GeneratedValue
	private Long id;
	@UpdateTimestamp
	private Instant updateDateTime;
	private String confirmationCode;
	private String loginURL;
	private String billingPlan = ApplicationBillingPlan.EMPTY.getCaption();
	@Enumerated(EnumType.STRING)
	private Source source;
	@Basic(optional = false)
	private boolean active = true;
	// поле создателя заявки
	private String creator;
	// APIKUB-773 поле работника (кто работает с заявкой)
	private String assignedTo;
	private Instant lastAttachmentDatetime;
	private Instant dateCreated;
	@LastModifiedDate
	private Instant lastUpdated;
	@Column(length = 2000)
	private String comment;
	@Type(type = "org.hibernate.type.TextType")
	private String contragents;
	/**
	 * ИНН контрагентов получателей
	 */
	@Type(type = "org.hibernate.type.TextType")
	private String contragentsRecip;
	private String income;
	private String taxForm;
	private String simplePassword;
	@Version
	private Integer entityVersion;

	private String clientTariffPlan;
	private Instant clientCallback;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(column = @Column(name = "client_name"), name = "name"),
			@AttributeOverride(column = @Column(name = "client_email"), name = "email"),
			@AttributeOverride(column = @Column(name = "client_address"), name = "address"),})
	private ClientValue client;

	/**
	 * приложенный паспорт
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(column = @Column(name = "passport_ser"), name = "ser"),
			@AttributeOverride(column = @Column(name = "passport_num"), name = "num"),
			@AttributeOverride(column = @Column(name = "person_snils"), name = "snils")
	})
	private PersonValue person;

	@Embedded
	private ChecksResultValue checks;

	@Embedded
	private StatusValue status;

	/**
	 * состояние, соответствующее набору состояний SM
	 */
	@Enumerated(EnumType.STRING)
	private ClientStates clientState;

	/**
	 * Данные счета
	 */
	@Embedded
	private AccountValue account;

	/**
	 * Виды контрактов
	 */
	@Embedded
	private Contracts contractTypes;

	@Embedded
	private CallBack callbackAt;

	@Embedded
	private QuestionnaireValue questionnaireValue;

	@OneToOne(optional = false)
	@NotNull
	private City city;
	@OneToOne
	private City firstSelectedCity;

	@Getter(onMethod = @__(@JsonIgnore))
	@JsonIgnore
	@OneToMany(orphanRemoval = true, mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@BatchSize(size = 20)
	private Set<HistoryItem> items;

	@Getter(onMethod = @__(@JsonIgnore))
	@JsonIgnore
	@OneToMany(orphanRemoval = true, mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@BatchSize(size = 10)
	private Set<StartWork> startWorkSet;

	@JsonIgnore
	transient private Set<Attachment> attachments;

	@JsonIgnore
	transient private Set<Attachment> bankAttachments;

	@OneToOne(cascade = CascadeType.ALL)
	private Utm utm;

	private Double aiScore; //вероятность открытия счета по версии ИИ

	AccountApplicationEntity(@Nonnull City city,
							 @Nonnull ClientValue client,
							 @Nonnull Source source) {
		this.city = city;
		this.firstSelectedCity = city;
		this.client = client;
		this.dateCreated = Instant.now();
		this.source = source;

		this.items = new HashSet<>();
		this.attachments = new HashSet<>();
		this.contractTypes = new Contracts();
		this.account = new AccountValue();
		this.checks = new ChecksResultValue();
		this.creator = SecurityContextHelper.getCurrentUsername();
		this.assignedTo = this.creator;
		this.clientState = ClientStates.NEW_CLIENT;
		this.status = new StatusValue(Status.CONTACT_INFO_UNCONFIRMED);

		log.info("Created new account application for mobile {} {}", client.getPhone(), (StringUtils.isNotBlank(client.getNumber()) ? (", ИНН " + client.getNumber()) : ""));
	}
}
