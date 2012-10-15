group sql;

drop_database(database) ::= <<
  DROP DATABASE <database>;
>>

create_database(database, owner, tablespace) ::= <<
  CREATE DATABASE <database> OWNER <owner>
                             ENCODING = 'utf8'
  <if(tablespace)> TABLESPACE = <tablespace><endif>;
>>

create_language() ::= <<
  CREATE LANGUAGE plpgsql;
>>

create_user(user, password) ::= <<
  CREATE ROLE <user> PASSWORD '<password>' LOGIN;
>>

create_schema(schema_name) ::= <<
  CREATE SCHEMA AUTHORIZATION <schema_name>;
>>

drop_schema(schema_name) ::= <<
  DROP SCHEMA <schema_name> CASCADE;
>>

detect_language() ::= <<
   SELECT count(*) FROM pg_language WHERE lanname='plpgsql';
>>

detect_user() ::= <<
   SELECT count(*) FROM pg_roles WHERE rolname=:user;
>>

detect_database() ::= <<
   SELECT count(*) FROM pg_database WHERE datname=:database;
>>

detect_tablespace() ::= <<
  SELECT count(*) FROM pg_tablespace WHERE spcname=:table_space;
>>

detect_schema() ::= <<
  SELECT count(*) FROM pg_namespace WHERE nspname=:schema_name;
>>
