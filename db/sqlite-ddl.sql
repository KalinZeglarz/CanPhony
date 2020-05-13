-- we don't know how to generate root <with-no-name> (class Root) :(
create table accounts
(
    id_account   integer not null
        constraint accounts_pk
            primary key autoincrement,
    username     text    not null,
    password     text    not null,
    ipv4_address text
);

create unique index accounts_id_account_uindex
    on accounts (id_account);

create unique index accounts_username_uindex
    on accounts (username);