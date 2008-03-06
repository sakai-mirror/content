merge into CONTENT_RESOURCE
	using dual on (dual.dummy is not null and CONTENT_RESOURCE.BINARY_ENTITY is not NULL)
		when matched then
			update set CONTENT_RESOURCE.XML=NULL;

merge into CONTENT_COLLECTION
	using dual on (dual.dummy is not null and CONTENT_COLLECTION.BINARY_ENTITY is not NULL)
		when matched then
			update set CONTENT_COLLECTION.XML=NULL;

drop table CONTENT_RES_T1REGISTER;
drop table CONTENT_COL_T1REGISTER;