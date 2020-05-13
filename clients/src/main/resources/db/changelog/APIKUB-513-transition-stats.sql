--liquibase formatted sql

-- changeset a.bubnov:1
create schema statistics;

create table statistics.status_transitions(
    timeslice timestamp not null,
    status_category text not null,
    city_id bigint not null,
    t_from bigint not null default 0,
    t_to bigint not null default 0,
    last_total bigint not null default 0,

    primary key (timeslice, status_category, city_id),
    foreign key (city_id) references city(id)
);
