-- init.sql
--
-- PART ONE
-- 
-- This SQL adds new columns and indexes to the content tables.
--
-- These statements should be executed before starting up the  
-- new version of ContentHostingService.   
--
alter table CONTENT_RESOURCE add CONTEXT VARCHAR2(99) default null;
alter table CONTENT_RESOURCE add FILE_SIZE NUMBER(18) default null;
alter table CONTENT_RESOURCE add RESOURCE_TYPE_ID VARCHAR2(255) default null;
alter table CONTENT_RESOURCE add BINARY_ENTITY BLOB;

CREATE INDEX CONTENT_RESOURCE_CI ON CONTENT_RESOURCE (CONTEXT);
CREATE INDEX CONTENT_RESOURCE_RTI ON CONTENT_RESOURCE (RESOURCE_TYPE_ID);
create index CONTENT_RESOURCE_FSI on CONTENT_RESOURCE(FILE_SIZE,0);

alter table CONTENT_COLLECTION add BINARY_ENTITY BLOB;

CREATE TABLE CONTENT_DROPBOX_CHANGES (DROPBOX_ID VARCHAR2 (255) NOT NULL, IN_COLLECTION VARCHAR2 (255), LAST_UPDATE VARCHAR2 (24));
CREATE UNIQUE INDEX CONTENT_DROPBOX_CI ON CONTENT_DROPBOX_CHANGES (DROPBOX_ID);
CREATE INDEX CONTENT_DROPBOX_II ON CONTENT_DROPBOX_CHANGES (IN_COLLECTION);

alter table CONTENT_RESOURCE_DELETE add CONTEXT VARCHAR2(99) default null;
alter table CONTENT_RESOURCE_DELETE add FILE_SIZE NUMBER(18) default null;
alter table CONTENT_RESOURCE_DELETE add RESOURCE_TYPE_ID VARCHAR2(255) default null;
alter table CONTENT_RESOURCE_DELETE add BINARY_ENTITY BLOB;

--
-- PART TWO
--
-- This SQL creates and populates the register tables for conversion 
-- of the CONTENT_RESOURCE and CONTENT_COLLECTION tables. 
-- 
-- These statements should be executed after starting up the new
-- version of ContentHostingService but before starting the 
-- conversion utility.
--
create table CONTENT_RES_T1REGISTER ( id VARCHAR2(1024), status VARCHAR2(99) );
create unique index CONTENT_RES_T1REGISTER_id_idx on CONTENT_RES_T1REGISTER(id);
create index CONTENT_RES_T1REGISTER_st_idx on CONTENT_RES_T1REGISTER(status);

INSERT INTO CONTENT_RES_T1REGISTER  (id, status)
  SELECT RESOURCE_ID, 'pending'
       FROM CONTENT_RESOURCE source_table
       WHERE NOT exists (select id
                  FROM CONTENT_RES_T1REGISTER register_table where source_table.RESOURCE_ID=register_table.id);

commit;

create table CONTENT_COL_T1REGISTER ( id VARCHAR2(1024), status VARCHAR2(99) );
create index CONTENT_COL_T1REGISTER_id_idx on CONTENT_COL_T1REGISTER(id);
create index CONTENT_COL_T1REGISTER_st_idx on CONTENT_COL_T1REGISTER(status);

INSERT INTO CONTENT_COL_T1REGISTER (id, status)
  SELECT collection_id, 'pending'
       FROM CONTENT_COLLECTION source_table
       WHERE NOT exists (select id
                  FROM CONTENT_COL_T1REGISTER register_table where source_table.collection_id=register_table.id);

commit;

create table CONTENT_CONVERSION_ERRORS ( entity_id VARCHAR2(255), conversion VARCHAR2(255), error_description VARCHAR2(1024), report_time TIMESTAMP default LOCALTIMESTAMP );


