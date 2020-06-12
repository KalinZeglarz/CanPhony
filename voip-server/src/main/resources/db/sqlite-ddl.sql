-- we don't know how to generate root <with-no-name> (class Root) :(
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

create table calls
(
    id_call    integer not null
        constraint calls_pk
            primary key autoincrement,
    id_account integer not null
        references accounts,
    username   text    not null,
    call_date  text    not null,
    duration   integer default -1 not null
);

create unique index calls_call_id_uindex
    on calls (id_call);

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
