create table business_service
(
    id   BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

create table business_tariff
(
    id                  BIGINT PRIMARY KEY,
    name                TEXT   NOT NULL,
    available_demo      BOOLEAN,
    available_payed     BOOLEAN,
    description         TEXT,
    parameters          TEXT,
    duration_demo       TEXT,
    duration_payed      TEXT,
    business_service_id BIGINT NOT NULL,

    FOREIGN KEY (business_service_id) REFERENCES business_service (id)
);

create table client_service_available
(
    id                  BIGINT PRIMARY KEY,
    client_id           BIGINT NOT NULL,
    business_service_id BIGINT NOT NULL,
    available           BOOLEAN,

    FOREIGN KEY (business_service_id) REFERENCES business_service (id)
);

create table client_business_tariff
(

    id                          BIGINT PRIMARY KEY,
    client_service_available_id BIGINT NOT NULL,
    business_tariff_id          BIGINT NOT NULL,
    is_demo                     BOOLEAN,
    is_payed                    BOOLEAN,
    date_begin                  TIMESTAMP,
    date_end                    TIMESTAMP,
    date_close                    TIMESTAMP,

    FOREIGN KEY (client_service_available_id) REFERENCES client_service_available (id),
    FOREIGN KEY (business_tariff_id) REFERENCES business_tariff (id)
);

insert into business_service (id, name)
values (NEXTVAL('hibernate_sequence'), 'Мое дело');

insert into business_tariff (id, name, available_demo, available_payed, description, parameters, duration_demo,
                             duration_payed, business_service_id)
values (NEXTVAL('hibernate_sequence'),
        'Просто. Бухгалтерия Мини',
        true,
        true,
        'Просто. Бухгалтерия Мини',
        'Просто. Бухгалтерия Мини',
        'THREE_DAYS',
        'MONTH', (select business_service.id from business_service where business_service.name = 'Мое дело'));

insert into business_tariff (id, name, available_demo, available_payed, description, parameters, duration_demo,
                             duration_payed, business_service_id)
values (NEXTVAL('hibernate_sequence'),
        'Просто. Бухгалтерия Эконом',
        true,
        true,
        'Просто. Бухгалтерия Эконом',
        'Просто. Бухгалтерия Эконом',
        'MONTH',
        'YEAR', (select business_service.id from business_service where business_service.name = 'Мое дело'));

insert into business_tariff (id, name, available_demo, available_payed, description, parameters, duration_demo,
                             duration_payed, business_service_id)
values (NEXTVAL('hibernate_sequence'),
        'Просто. Бухгалтерия Комфорт',
        true,
        true,
        'Просто. Бухгалтерия Комфорт',
        'Просто. Бухгалтерия Комфорт',
        'MONTH',
        'YEAR', (select business_service.id from business_service where business_service.name = 'Мое дело'));

insert into business_tariff (id, name, available_demo, available_payed, description, parameters, duration_demo,
                             duration_payed, business_service_id)
values (NEXTVAL('hibernate_sequence'),
        'Просто. Бухгалтерия Премиум',
        true,
        true,
        'Просто. Бухгалтерия Премиум',
        'Просто. Бухгалтерия Премиум',
        'MONTH',
        'YEAR', (select business_service.id from business_service where business_service.name = 'Мое дело'));


insert into business_tariff (id, name, available_demo, available_payed, description, parameters, duration_demo,
                             duration_payed, business_service_id)
values (NEXTVAL('hibernate_sequence'),
        'Просто. Бухгалтерия Премиум ОСНО',
        true,
        true,
        'Просто. Бухгалтерия Премиум ОСНО',
        'Просто. Бухгалтерия Премиум ОСНО',
        'MONTH',
        'YEAR', (select business_service.id from business_service where business_service.name = 'Мое дело'));