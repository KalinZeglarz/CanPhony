-- we don't know how to generate schema main (class Schema) :(
create table accounts
(
    id_account   integer not null
        constraint accounts_pk
            primary key autoincrement,
    username     text    not null,
    password     text    not null,
    ipv4_address text,
    status       text
);

create unique index accounts_id_account_uindex
    on accounts (id_account);

create unique index accounts_username_uindex
    on accounts (username);

create table password_policy
(
    id_policy    integer not null
        constraint password_policy_pk
            primary key autoincrement,
    policy_name  text    not null,
    policy_value text
);

create unique index password_policy_id_policy_uindex
    on password_policy (id_policy);

create unique index password_policy_policy_name_uindex
    on password_policy (policy_name);
