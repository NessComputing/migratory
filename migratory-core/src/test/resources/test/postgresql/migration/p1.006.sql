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

CREATE TABLE p1_table_d (
    col_o                    TIMESTAMP WITH TIME ZONE,
    col_d                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    col_f                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    p1_table_d_id    BIGINT NOT NULL DEFAULT nextval('p1_seq') PRIMARY KEY
);

