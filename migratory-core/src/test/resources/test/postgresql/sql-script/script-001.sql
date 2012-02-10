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
ALTER TABLE sometable 
    ADD last_modified TIMESTAMP;
 
ALTER TABLE sometable 
    ALTER COLUMN last_modified 
        SET DEFAULT CURRENT_TIMESTAMP;
 
UPDATE sometable
    SET last_modified=CURRENT_TIMESTAMP;
 
CREATE OR REPLACE FUNCTION update_lastmodified_column()
        RETURNS TRIGGER AS '
  BEGIN
    NEW.last_modified = NOW();
    RETURN NEW;
  END;
' LANGUAGE 'plpgsql';
 
CREATE TRIGGER update_lastmodified_modtime BEFORE UPDATE
  ON sometable FOR EACH ROW EXECUTE PROCEDURE
  update_lastmodified_column();
  
