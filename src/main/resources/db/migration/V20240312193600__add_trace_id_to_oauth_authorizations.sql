ALTER TABLE oauth_authorizations
ADD COLUMN trace_id varchar(256) DEFAULT NULL; 