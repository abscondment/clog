create table posts (
       id    integer primary key,
       title varchar(128),
       body  text,
       url   varchar(128),
       created_at text
);
create unique index seourl on posts (url);