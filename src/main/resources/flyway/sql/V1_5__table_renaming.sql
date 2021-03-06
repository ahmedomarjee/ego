ALTER TABLE ACLENTITY RENAME TO POLICY;
ALTER TABLE POLICY RENAME CONSTRAINT ACLENTITY_PKEY TO POLICY_PKEY;
ALTER TABLE POLICY RENAME CONSTRAINT ACLENTITY_NAME_KEY TO POLICY_NAME_KEY;
ALTER TABLE POLICY RENAME CONSTRAINT ACLENTITY_OWNER_FKEY TO POLICY_OWNER_FKEY;

ALTER TABLE ACLUSERPERMISSION RENAME TO USERPERMISSION;
ALTER TABLE USERPERMISSION RENAME ENTITY TO POLICY_ID;
ALTER TABLE USERPERMISSION RENAME SID TO USER_ID;
ALTER TABLE USERPERMISSION RENAME MASK TO ACCESS_LEVEL;
ALTER TABLE USERPERMISSION RENAME CONSTRAINT ACLUSERPERMISSION_PKEY TO USERPERMISSION_PKEY;
ALTER TABLE USERPERMISSION RENAME CONSTRAINT ACLUSERPERMISSION_ENTITY_FKEY TO USERPERMISSION_POLICY_FKEY;
ALTER TABLE USERPERMISSION RENAME CONSTRAINT ACLUSERPERMISSION_SID_FKEY TO USERPERMISSION_USER_FKEY;

ALTER TABLE ACLGROUPPERMISSION RENAME TO GROUPPERMISSION;
ALTER TABLE GROUPPERMISSION RENAME ENTITY TO POLICY_ID;
ALTER TABLE GROUPPERMISSION RENAME SID TO GROUP_ID;
ALTER TABLE GROUPPERMISSION RENAME MASK TO ACCESS_LEVEL;

ALTER TABLE GROUPPERMISSION RENAME CONSTRAINT ACLGROUPPERMISSION_PKEY TO GROUPPERMISSION_PKEY;
ALTER TABLE GROUPPERMISSION RENAME CONSTRAINT ACLGROUPPERMISSION_ENTITY_FKEY TO GROUPPERMISSION_POLICY_FKEY;
ALTER TABLE GROUPPERMISSION RENAME CONSTRAINT ACLGROUPPERMISSION_SID_FKEY TO GROUPPERMISSION_GROUP_FKEY;

ALTER TABLE USERGROUP RENAME USERID TO USER_ID;
ALTER TABLE USERGROUP RENAME GRPID TO GROUP_ID;
ALTER TABLE USERGROUP RENAME CONSTRAINT USERGROUP_GRPID_FKEY TO USERGROUP_GROUP_FKEY;
ALTER TABLE USERGROUP RENAME CONSTRAINT USERGROUP_USERID_FKEY TO USERGROUP_USER_FKEY;

ALTER TABLE USERAPPLICATION RENAME USERID TO USER_ID;
ALTER TABLE USERAPPLICATION RENAME APPID TO APPLICATION_ID;
ALTER TABLE USERAPPLICATION RENAME CONSTRAINT USERAPPLICATION_APPID_FKEY TO USERAPPLICATION_APPLICATION_FKEY;
ALTER TABLE USERAPPLICATION RENAME CONSTRAINT USERAPPLICATION_USERID_FKEY TO USERAPPLICATION_USER_FKEY;

ALTER TABLE GROUPAPPLICATION RENAME GRPID TO GROUP_ID;
ALTER TABLE GROUPAPPLICATION RENAME APPID TO APPLICATION_ID;
ALTER TABLE GROUPAPPLICATION RENAME CONSTRAINT GROUPAPPLICATION_APPID_FKEY TO GROUPAPPLICATION_APPLICATION_FKEY;
ALTER TABLE GROUPAPPLICATION RENAME CONSTRAINT GROUPAPPLICATION_GRPID_FKEY TO GROUPAPPLICATION_GROUP_FKEY;

ALTER TABLE TOKENAPPLICATION RENAME TOKENID TO TOKEN_ID;
ALTER TABLE TOKENAPPLICATION RENAME APPID TO APPLICATION_ID;
ALTER TABLE TOKENAPPLICATION RENAME CONSTRAINT TOKENAPPLICATION_APPID_FKEY TO TOKENAPPLICATION_APPLICATION_FKEY;
ALTER TABLE TOKENAPPLICATION RENAME CONSTRAINT TOKENAPPLICATION_TOKENID_FKEY TO TOKENAPPLICATION_TOKEN_FKEY;

ALTER TABLE TOKENSCOPE RENAME CONSTRAINT TOKENSCOPE_POLICY_ID_FKEY TO TOKENSCOPE_POLICY_FKEY;
ALTER TABLE TOKENSCOPE RENAME CONSTRAINT TOKENSCOPE_TOKEN_ID_FKEY TO TOKENSCOPE_TOKEN_FKEY;