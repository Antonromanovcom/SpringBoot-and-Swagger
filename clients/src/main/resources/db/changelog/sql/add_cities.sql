alter table account_application
    add column first_selected_city_id bigint;

alter table city
    alter column to_list set default true;
alter table city
    add column is_serviced boolean default true;
alter table city
    alter column is_serviced set default false;
update city
set is_serviced = false
where name like '%еизвестно';

insert into city (id, name)
values (NEXTVAL('hibernate_sequence'), 'Саратов'),
       (NEXTVAL('hibernate_sequence'), 'Тюмень'),
       (NEXTVAL('hibernate_sequence'), 'Тольятти'),
       (NEXTVAL('hibernate_sequence'), 'Ижевск'),
       (NEXTVAL('hibernate_sequence'), 'Барнаул'),
       (NEXTVAL('hibernate_sequence'), 'Ульяновск'),
       (NEXTVAL('hibernate_sequence'), 'Иркутск'),
       (NEXTVAL('hibernate_sequence'), 'Хабаровск'),
       (NEXTVAL('hibernate_sequence'), 'Ярославль'),
       (NEXTVAL('hibernate_sequence'), 'Владивосток'),
       (NEXTVAL('hibernate_sequence'), 'Махачкала'),
       (NEXTVAL('hibernate_sequence'), 'Томск'),
       (NEXTVAL('hibernate_sequence'), 'Оренбург'),
       (NEXTVAL('hibernate_sequence'), 'Кемерово'),
       (NEXTVAL('hibernate_sequence'), 'Новокузнецк'),
       (NEXTVAL('hibernate_sequence'), 'Рязань'),
       (NEXTVAL('hibernate_sequence'), 'Астрахань'),
       (NEXTVAL('hibernate_sequence'), 'Набережные Челны'),
       (NEXTVAL('hibernate_sequence'), 'Пенза'),
       (NEXTVAL('hibernate_sequence'), 'Липецк'),
       (NEXTVAL('hibernate_sequence'), 'Киров'),
       (NEXTVAL('hibernate_sequence'), 'Чебоксары'),
       (NEXTVAL('hibernate_sequence'), 'Тула'),
       (NEXTVAL('hibernate_sequence'), 'Калининград'),
       (NEXTVAL('hibernate_sequence'), 'Балашиха'),
       (NEXTVAL('hibernate_sequence'), 'Курск'),
       (NEXTVAL('hibernate_sequence'), 'Севастополь'),
       (NEXTVAL('hibernate_sequence'), 'Улан-Удэ'),
       (NEXTVAL('hibernate_sequence'), 'Ставрополь'),
       (NEXTVAL('hibernate_sequence'), 'Сочи'),
       (NEXTVAL('hibernate_sequence'), 'Тверь'),
       (NEXTVAL('hibernate_sequence'), 'Магнитогорск'),
       (NEXTVAL('hibernate_sequence'), 'Иваново'),
       (NEXTVAL('hibernate_sequence'), 'Брянск');
