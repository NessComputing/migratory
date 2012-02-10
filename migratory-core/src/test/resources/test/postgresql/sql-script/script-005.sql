--
-- Copyright (C) 2012 Ness Computing, Inc.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

LOCK sometable;
CREATE SEQUENCE sometable_last_modified_seq;
ALTER TABLE sometable ALTER last_modified DROP DEFAULT;
ALTER TABLE sometable 
    ALTER last_modified TYPE BIGINT USING 0;

ALTER TABLE sometable
    ALTER last_modified SET DEFAULT nextval('sometable_last_modified_seq');
ALTER SEQUENCE sometable_last_modified_seq OWNED BY sometable.last_modified;
 
CREATE OR REPLACE FUNCTION update_lastmodified_column()
        RETURNS TRIGGER AS '
  BEGIN
    NEW.last_modified = nextval(''sometable_last_modified_seq'');
    RETURN NEW;
  END;
' LANGUAGE 'plpgsql';
 
-- some random comment
-- some random comment
-- some random comment

UPDATE sometable SET id=id WHERE id=1;
