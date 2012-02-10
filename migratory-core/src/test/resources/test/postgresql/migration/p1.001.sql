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
-- random comment
CREATE SEQUENCE p1_seq START 1000;

-- random comment
-- random comment
-- random comment
CREATE TABLE p1_table_b (
    col_a             BIGINT NOT NULL,
    col_b            CHARACTER VARYING NOT NULL,
    col_c        CHARACTER VARYING NOT NULL DEFAULT 'US',
    col_d             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    col_f             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    p1_table_b_id    BIGINT NOT NULL DEFAULT nextval('p1_seq') PRIMARY KEY,

    UNIQUE(col_a, col_b, col_c)
);

CREATE INDEX p1_table_b_zip ON p1_table_b(col_b);

-- random comment
-- random comment
-- random comment
CREATE TABLE p1_table_a (
    col_g         BIGINT NOT NULL,
    col_h    BIGINT NOT NULL,
    col_i     BIGINT NOT NULL,
    col_j        prereq_type NOT NULL,
    col_d                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    col_f                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    p1_table_a_id  BIGINT NOT NULL DEFAULT nextval('p1_seq') PRIMARY KEY,

    UNIQUE(col_g, col_i, col_j)
);

CREATE INDEX p1_table_a_col_g ON p1_table_a(col_g);
CREATE INDEX p1_table_a_col_h ON p1_table_a(col_h);
CREATE INDEX p1_table_a_col_ij ON p1_table_a(col_i, col_j);
