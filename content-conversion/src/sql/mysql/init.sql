alter table CONTENT_RESOURCE add CONTEXT VARCHAR(99) default null;
alter table CONTENT_RESOURCE add FILE_SIZE BIGINT default null;
alter table CONTENT_RESOURCE add RESOURCE_TYPE_ID VARCHAR(255) default null;
alter table CONTENT_RESOURCE add BINARY_ENTITY BLOB;

create table CONTENT_RES_T1REGISTER ( id varchar(1024), status varchar(99) );
create unique index CONTENT_RES_T1REGISTER_id_idx on CONTENT_RES_T1REGISTER(id);
create index CONTENT_RES_T1REGISTER_st_idx on CONTENT_RES_T1REGISTER(status);
create index content_resource_file_size_idx on CONTENT_RESOURCE(FILE_SIZE);

insert into CONTENT_RES_T1REGISTER (id,status) select RESOURCE_ID, 'pending' from CONTENT_RESOURCE where BINARY_ENTITY is NULL and XML is not null;


-----------------------------------------------------------------------------
-- CONTENT_DROPBOX_CHANGES
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_DROPBOX_CHANGES
(
    DROPBOX_ID VARCHAR (255) NOT NULL,
    IN_COLLECTION VARCHAR (255),
    LAST_UPDATE VARCHAR (24)
);

CREATE UNIQUE INDEX CONTENT_DROPBOX_CI ON CONTENT_DROPBOX_CHANGES
(
	DROPBOX_ID
);

CREATE INDEX CONTENT_DROPBOX_II ON CONTENT_DROPBOX_CHANGES
(
	IN_COLLECTION
);
