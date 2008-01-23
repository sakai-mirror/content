alter table CONTENT_RESOURCE add CONTEXT VARCHAR(99) default null;
alter table CONTENT_RESOURCE add FILE_SIZE BIGINT default null;
alter table CONTENT_RESOURCE add RESOURCE_TYPE_ID VARCHAR(255) default null;
alter table CONTENT_RESOURCE add BINARY_ENTITY BLOB;

create table CONTENT_RES_T1REGISTER ( id varchar(1024), status varchar(99) );
create unique index CONTENT_RES_T1REGISTER_id_idx on CONTENT_RES_T1REGISTER(id);
create index CONTENT_RES_T1REGISTER_st_idx on CONTENT_RES_T1REGISTER(status);
insert into CONTENT_RES_T1REGISTER (id,status) select RESOURCE_ID, 'pending' from CONTENT_RESOURCE where BINARY_ENTITY is NULL and XML is not null;
