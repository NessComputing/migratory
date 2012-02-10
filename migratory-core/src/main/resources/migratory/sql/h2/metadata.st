group sql;

insert_metadata(table_name) ::= <<
  INSERT into <table_name>
    (start_version, end_version, personality_name, description, type, state, direction, execution_time, location, script_name, checksum)
  VALUES
    (:start_version, :end_version, :personality_name, :description, :type, :state, :direction, :execution_time, :location, :script_name, :checksum);
>>

query_status(table_name, personality_name, include_internal, include_failed) ::= <<
  SELECT * FROM <table_name> WHERE metadata_id IN (
    SELECT MAX(metadata_id) FROM <table_name>
    WHERE 1 = 1
          <if(!include_failed)>AND state = '<state_ok>'<endif>
          <if(!include_internal)>AND personality_name \<\> '<metadata_name>'<endif>
          <if(personality_name)>AND personality_name = :personality_name<endif>
    GROUP BY personality_name
  )
>>

query_history(table_name, personality_name, include_internal) ::= <<
  SELECT * FROM <table_name> WHERE 1 = 1
    <if(personality_name)>AND personality_name = :personality_name<endif>
    <if(!include_internal)>AND personality_name \<\> '<metadata_name>'<endif>
    ORDER BY metadata_id ASC;
>>

lock(table_name, personality_name) ::= <<
  SELECT metadata_id FROM <table_name> WHERE personality_name = :personality_name FOR UPDATE;
>>
