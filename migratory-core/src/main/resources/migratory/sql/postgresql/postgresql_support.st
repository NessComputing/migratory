group sql;

current_schema() ::= <<
  SELECT current_schema();
>>

find_tables() ::= <<
  SELECT tablename FROM pg_tables WHERE schemaname = current_schema();
>>

drop_tables(tablename) ::= <<
  DROP TABLE IF EXISTS <tablename> CASCADE;
>>

find_views() ::= <<
  SELECT viewname FROM pg_views WHERE schemaname = current_schema();
>>

drop_views(viewname) ::= <<
  DROP VIEW IF EXISTS <viewname> CASCADE;
>>

find_seqs() ::= <<
  SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=current_schema();
>>

drop_seqs(sequence_name) ::= <<
  DROP SEQUENCE IF EXISTS <sequence_name> CASCADE;
>>

find_triggers() ::= <<
  SELECT trigger_name,event_object_table FROM information_schema.triggers WHERE trigger_schema=current_schema();
>>

drop_triggers(trigger_name, event_object_table) ::= <<
  DROP TRIGGER IF EXISTS <trigger_name> ON <event_object_table> CASCADE;
>>

find_functions() ::= <<
  SELECT a.routine_name AS name, oidvectortypes(proargtypes) AS args FROM information_schema.routines AS a, pg_proc
   WHERE a.routine_name = proname AND a.routine_schema = current_schema();
>>

drop_functions(name, args) ::= <<
  DROP FUNCTION IF EXISTS <name> ( <args> ) CASCADE;
>>

find_types() ::= <<
  SELECT typname FROM pg_type t LEFT JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace
                 WHERE (t.typrelid = 0 OR (SELECT c.relkind = 'c' FROM pg_catalog.pg_class c WHERE c.oid = t.typrelid))
                   AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type el
                                          WHERE el.oid = t.typelem
                                            AND el.typarray = t.oid)
                                            AND n.nspname NOT IN ('pg_catalog', 'information_schema');
>>

drop_types(typname) ::= <<
  DROP TYPE IF EXISTS <typname> CASCADE;
>>
