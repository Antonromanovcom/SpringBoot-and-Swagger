-----------------------------------------------------
--              Create custom users                --
-----------------------------------------------------
create database pos;
create user posuser;
alter user posuser with encrypted password 'pospass';
grant all privileges on database pos to posuser;

-- lack of schema for:
create database isimplelab;
create user isimplelab;
alter user isimplelab with encrypted password 'isimplelab';
grant all privileges on database isimplelab to isimplelab;

------------------------------------------------------
--  Postgres db structure for application-dev-local --
------------------------------------------------------
create sequence IF NOT EXISTS hibernate_sequence start with 1 increment by 1;
create table account_application (id bigint not null, account_number varchar(255), bank_id integer, appointment_address varchar(255), when_date date, when_time time, billing_plan varchar(255), kontur_check double precision, kontur_error_text varchar(255), p550check varchar(255), smev_check varchar(255), client_address varchar(255), client_email varchar(255), head varchar(255), inn varchar(255), client_name varchar(255), ogrn varchar(255), phone varchar(255), comment varchar(2000), confirmation_code varchar(255), actives boolean not null, lend boolean not null, loan boolean not null, market boolean not null, other boolean not null, other_text varchar(255), produce boolean not null, service boolean not null, subcontract boolean not null, contragents varchar(2000), date_created timestamp, entity_version integer, income varchar(255), loginurl varchar(255), partner_tag varchar(255), dob date, doi date, issuer varchar(255), issuer_code varchar(255), passport_num varchar(255), pob varchar(255), passport_ser varchar(255), person_snils varchar(255), simple_password varchar(255), callback_at timestamp, status varchar(255), subcode integer, tax_form varchar(255), city_id bigint not null, primary key (id));
create table attachment (id bigint not null, att_type integer, attachment_name varchar(255), content oid, created_at timestamp, primary key (id));
create table attachment_bank (account_application_id bigint not null, bank_attachments_id bigint not null, primary key (account_application_id, bank_attachments_id));
create table attachment_user (account_application_id bigint not null, attachments_id bigint not null, primary key (account_application_id, attachments_id));
create table city (id bigint not null, name varchar(255), primary key (id));
create table history_item (id bigint not null, created_at timestamp, item_type integer, text varchar(2000), app_id bigint not null, primary key (id));
create table passport_issuers (id bigint not null, code varchar(255), name varchar(255), primary key (id));
alter table attachment_bank add constraint UK_l9x9bvxte8jfjp9vq7j1oj3aq unique (bank_attachments_id);
alter table attachment_user add constraint UK_rpi94932d1ypks7aevk5na4h0 unique (attachments_id);
alter table account_application add constraint FKfxp6eexxq381dmt2vmgcw6no1 foreign key (city_id) references city;
alter table attachment_bank add constraint FKicoqqq1dwnppl3qfliyyr9bvq foreign key (bank_attachments_id) references attachment;
alter table attachment_bank add constraint FKlklybgsqmqcxqheb9his03pxq foreign key (account_application_id) references account_application;
alter table attachment_user add constraint FKpp9v5nq77upfn3g64op3yhpsf foreign key (attachments_id) references attachment;
alter table attachment_user add constraint FKdn2kacdv80ttqqe473h6kgkrb foreign key (account_application_id) references account_application;
alter table history_item add constraint FKn6caeoff257dqol2pincprefr foreign key (app_id) references account_application;
