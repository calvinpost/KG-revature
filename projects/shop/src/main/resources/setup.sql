-- create schema shop;
-- set schema 'shop';

drop table if exists "user" cascade;
drop type if exists role;
create type role as enum ('customer', 'employee', 'manager');
create table "user" (
    user_id serial primary key,
    assigned_role role
);

drop table if exists item cascade;
drop type if exists stock;
create type stock as enum ('available', 'owned');
create table item (
    item_id serial primary key,
    item_name varchar(20),
    stock stock
);

drop table if exists offer cascade;
drop type if exists status;
create type status as enum ('pending', 'accepted');
create table offer (
    user_id integer references "user"(user_id),
    item_id integer references item(item_id),
    offer_date date default current_date,
    offer_amount numeric(2),
    status status,
    primary key (user_id, item_id)
)