create table CONTENT_CONVERSION_ERRORS ( entity_id VARCHAR2(255), conversion VARCHAR2(255), error_description VARCHAR2(1024), report_time TIMESTAMP default LOCALTIMESTAMP );

alter table CONTENT_RESOURCE add CONTEXT VARCHAR2(99) default null;
alter table CONTENT_RESOURCE add FILE_SIZE NUMBER(18) default null;
alter table CONTENT_RESOURCE add REsource_table_TYPE_ID VARCHAR2(255) default null;
alter table CONTENT_RESOURCE add BINARY_ENTITY BLOB;

create table CONTENT_RES_T1REGISTER ( id VARCHAR2(1024), status VARCHAR2(99) );
create unique index CONTENT_RES_T1REGISTER_id_idx on CONTENT_RES_T1REGISTER(id);
create index CONTENT_RES_T1REGISTER_st_idx on CONTENT_RES_T1REGISTER(status);
INSERT INTO CONTENT_RES_T1REGISTER  (id, status)
  SELECT resource_table_id, 'pending'
       FROM CONTENT_RESOURCE source_table
       WHERE NOT exists (select id
                  FROM CONTENT_RES_T1REGISTER register_table where source_table.resource_table_id=register_table.id); 
--insert into CONTENT_RES_T1REGISTER (id,status) select REsource_table_ID, 'pending' from CONTENT_RESOURCE where resource_table_id not in (select id from CONTENT_RES_T1REGISTER);

commit;

alter table CONTENT_RESOURCE_DELETE add CONTEXT VARCHAR2(99) default null;
alter table CONTENT_RESOURCE_DELETE add FILE_SIZE NUMBER(18) default null;
alter table CONTENT_RESOURCE_DELETE add REsource_table_TYPE_ID VARCHAR2(255) default null;
alter table CONTENT_RESOURCE_DELETE add BINARY_ENTITY BLOB;

create table CONTENT_DEL_T1REGISTER ( id VARCHAR2(1024), status VARCHAR2(99) );
create  index CONTENT_DEL_T1REGISTER_id_idx on CONTENT_DEL_T1REGISTER(id);
create index CONTENT_DEL_T1REGISTER_st_idx on CONTENT_DEL_T1REGISTER(status);
INSERT INTO CONTENT_DEL_T1REGISTER  (id, status)
  SELECT resource_table_id, 'pending'
       FROM CONTENT_RESOURCE_DELETE source_table
       WHERE NOT exists (select id
                  FROM CONTENT_DEL_T1REGISTER register_table where source_table.resource_table_id=register_table.id); 
--insert into CONTENT_DEL_T1REGISTER (id,status) select REsource_table_ID, 'pending' from CONTENT_RESOURCE_DELETE where resource_table_id not in (select id from CONTENT_DEL_T1REGISTER);

commit;

alter table CONTENT_COLLECTION add BINARY_ENTITY BLOB;

create table CONTENT_COL_T1REGISTER ( id VARCHAR2(1024), status VARCHAR2(99) );
create index CONTENT_COL_T1REGISTER_id_idx on CONTENT_COL_T1REGISTER(id);
create index CONTENT_COL_T1REGISTER_st_idx on CONTENT_COL_T1REGISTER(status);
INSERT INTO content_col_T1REGISTER  (id, status)
  SELECT collection_id, 'pending'
       FROM content_collection source_table
       WHERE NOT exists (select id
                  FROM content_col_T1REGISTER register_table where source_table.collection_id=register_table.id); 
--insert into CONTENT_COL_T1REGISTER (id,status) select COLLECTION_ID, 'pending' from CONTENT_COLLECTION where COLLECTION_ID not in (select id from CONTENT_COL_T1REGISTER);

commit;
