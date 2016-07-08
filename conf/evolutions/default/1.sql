# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table swan_song_expression (
  expression_id             varchar(255) not null,
  token_id                  varchar(255),
  expression                varchar(255),
  constraint pk_swan_song_expression primary key (expression_id))
;




# --- !Downs

drop table if exists swan_song_expression cascade;

