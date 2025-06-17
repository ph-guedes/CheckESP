CREATE USER postgres SUPERUSER ENCRYPTED PASSWORD 'postgres';
CREATE DATABASE "checkesp"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.utf8'
       LC_CTYPE = 'en_US.utf8'
       CONNECTION LIMIT = -1;
GRANT ALL PRIVILEGES ON DATABASE checkesp TO postgres;