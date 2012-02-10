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

CREATE TYPE p1_table_c_type AS ENUM('A','B','C');

-- random comment
-- random comment
-- random comment
CREATE TABLE p1_table_c (
    col_g         BIGINT NOT NULL,
    col_k      BIGINT NOT NULL,
    col_l       CHARACTER VARYING(255),
    col_m          prereq_type NOT NULL,
    col_n           p1_table_c_type NOT NULL DEFAULT('B'),
    col_d                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    col_f                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    p1_table_c_id    BIGINT NOT NULL DEFAULT nextval('p1_seq') PRIMARY KEY,

    UNIQUE(col_g, col_k, col_l, col_m)
);

CREATE INDEX p1_table_c_col_g ON p1_table_c(col_g);
CREATE INDEX p1_table_c_col_k ON p1_table_c(col_k);
CREATE INDEX p1_table_c_col_lm ON p1_table_c(col_l, col_m);


