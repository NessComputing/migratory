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
CREATE OR REPLACE FUNCTION p1_table_c_upsert (
       in_col_g       BIGINT,
       in_col_k    BIGINT,
       in_col_l     CHARACTER VARYING,
       in_col_m        CHARACTER VARYING,
       in_col_n         CHARACTER VARYING
)
RETURNS INTEGER AS
$$
BEGIN
  LOOP
    UPDATE p1_table_c
    SET
      col_n = in_col_n::p1_table_c_type,
      col_f = NOW()
    WHERE col_g = in_col_g 
      AND col_k = in_col_k
      AND col_l = in_col_l
      AND col_m = in_col_m::prereq_type;
    IF found THEN
      RETURN 0;
    END IF;

    BEGIN
      INSERT INTO p1_table_c
        (col_g, col_k, col_l, col_m, col_n)
      VALUES
        (in_col_g, in_col_k, in_col_l, in_col_m::prereq_type, in_col_n::p1_table_c_type);
      RETURN 1;
    EXCEPTION WHEN unique_violation THEN
        -- random comment
    END;
  END LOOP;
END;
$$
LANGUAGE plpgsql;
