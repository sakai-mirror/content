-- This SQL is for mysql.  It is to copy the data from CONTENT_RESOURCE
-- and CONTENT_COLLECTION to the migration table. This is script is just for
-- testing.  This initial copying should be triggered by the migration code
-- when it first runs.

-- First copy over the folder/collection table.
INSERT INTO MIGRATE_CHS_CONTENT_TO_JCR 
  (CONTENT_ID)
  SELECT CONTENT_COLLECTION.COLLECTION_ID
  FROM CONTENT_COLLECTION;

-- Then copy over the file/resource table.
INSERT INTO MIGRATE_CHS_CONTENT_TO_JCR
  (CONTENT_ID)
  SELECT CONTENT_RESOURCE.RESOURCE_ID
  FROM CONTENT_RESOURCE;
