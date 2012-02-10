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

-- random comment
-- random comment
-- random comment
ALTER TABLE p1_table_a RENAME COLUMN col_i TO _old;
ALTER TABLE p1_table_a ADD column col_i CHARACTER VARYING(255);
UPDATE p1_table_a SET col_i = _old;
ALTER TABLE p1_table_a DROP COLUMN _old;
ALTER TABLE p1_table_a ALTER column col_i SET NOT NULL;

-- random comment
-- random comment
ALTER TABLE p1_table_a ADD CONSTRAINT p1_table_a_col_g_key  UNIQUE(col_g, col_i, col_j);
CREATE INDEX p1_table_a_source ON p1_table_a(col_i, col_j);

