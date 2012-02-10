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
CREATE OR REPLACE FUNCTION p1_upsert (
       in_col_g       BIGINT,
       in_col_h  BIGINT,
       in_col_i   CHARACTER VARYING,
       in_col_j      CHARACTER VARYING
)
RETURNS INTEGER AS
$$
BEGIN
  LOOP
    UPDATE p1_table_a
    SET
      col_f = NOW()
    WHERE col_g = in_col_g 
      AND col_h = in_col_h
      AND col_i = in_col_i
      AND col_j = in_col_j::prereq_type;
    IF found THEN
      RETURN 0;
    END IF;

    BEGIN
      INSERT INTO p1_table_a
        (col_g, col_h, col_i, col_j)
      VALUES
        (in_col_g, in_col_h, in_col_i, in_col_j::prereq_type);
      RETURN 1;
    EXCEPTION WHEN unique_violation THEN
        -- random comment
    END;
  END LOOP;
END;
$$
LANGUAGE plpgsql;

