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
CREATE SEQUENCE p2_table_seq START 1000;

-- random comment
-- random comment
-- random comment
CREATE TABLE p2_table (
    col_a     BIGINT NOT NULL,
    col_b   CHARACTER VARYING NOT NULL,
    col_c CHARACTER VARYING NOT NULL,
    col_d     TIMESTAMP WITH TIME ZONE NOT NULL,
    col_e     TIMESTAMP WITH TIME ZONE NOT NULL,
    p2_table_id BIGINT NOT NULL DEFAULT nextval('p2_table_seq') PRIMARY KEY,
    -- random comment
    UNIQUE(col_a, col_b)
);

-- random comment
-- random comment
-- random comment
CREATE FUNCTION p2_table_upsert (
       in_col_a       BIGINT,
       in_col_b           CHARACTER VARYING,
       in_col_c         CHARACTER VARYING
)
RETURNS INTEGER AS
$$
BEGIN
  LOOP
    UPDATE p2_table 
    SET
      col_c = in_col_c,
      col_e = NOW()
    WHERE col_a = in_col_a AND col_b = in_col_b;
    IF found THEN
      RETURN 0;
    END IF;

    BEGIN
      INSERT INTO p2_table
        (col_a, col_b, col_c, col_d, col_e, p2_table_id)
      VALUES
        (in_col_a, in_col_b, in_col_c, NOW(), NOW(), nextval('p2_table_seq'));
      RETURN 1;
    EXCEPTION WHEN unique_violation THEN
        -- random comment
    END;
  END LOOP;
END;
$$
LANGUAGE plpgsql;

