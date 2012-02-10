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

CREATE TABLE sometable (
       col_a sometype NOT NULL,
       col_a_id CHARACTER VARYING NOT NULL,
       col_a_col_l CHARACTER VARYING NOT NULL,
       col_b BOOLEAN NOT NULL,
       col_c BOOLEAN NOT NULL,
       col_d TIMESTAMP WITH TIME ZONE NOT NULL,
       col_e CHARACTER VARYING (256),
       col_f CHARACTER VARYING (1024),
       col_g CHARACTER VARYING (256),
       col_h CHARACTER VARYING NOT NULL,
       col_i foo_col_i NOT NULL,
       extended_col_i CHARACTER VARYING,
       PRIMARY KEY (col_a, col_a_id)
);
CREATE INDEX sometable_col_d_idx on sometable(col_d);

CREATE TABLE sometable2 (
 -- this is some random comment.
    col_a sometype NOT NULL,
    col_a_place_id CHARACTER VARYING NOT NULL,
 -- this is some random comment.
    col_h_col_a sometype,
    col_h_id CHARACTER VARYING,
 -- this is some random comment.
    col_j BIGINT,
 -- this is some random comment.
    col_d TIMESTAMP WITH TIME ZONE NOT NULL,
    col_l BIGINT NOT NULL
);

-- this is some random comment.
CREATE INDEX sometable2_place_id_idx ON sometable2 (col_j)
WHERE col_j IS NOT NULL;

-- this is some random comment.
-- this is some random comment.
CREATE TABLE sometable3 (
       col_a sometype NOT NULL,
       col_a_id CHARACTER VARYING NOT NULL,
       col_k TIMESTAMP WITH TIME ZONE NOT NULL,
 -- this is some random comment.
 CONSTRAINT pk_sometable3 PRIMARY KEY (col_a, col_a_id)
);

CREATE TABLE sometable4 (
       id serial,
       col_m bigint NOT NULL,
       col_h_col_a sometype NOT NULL,
       col_h_col_a_id character varying NOT NULL,
 
 CONSTRAINT pk_sometable4 PRIMARY KEY (col_m, col_h_col_a,
 col_h_col_a_id)
 
);

CREATE INDEX sometable4_m_idx ON sometable4 (col_m);
CREATE INDEX sometable4_p_idx ON sometable4 (col_h_col_a, col_h_col_a_id);
ALTER TABLE sometable4 ADD CONSTRAINT fk_sometable5
 FOREIGN KEY (col_h_col_a, col_h_col_a_id)
 REFERENCES sometable (col_a, col_a_id);
 

CREATE OR REPLACE FUNCTION merge_sometable3(
 a sometype, -- this is some random comment.
 b CHARACTER VARYING -- this is some random comment.
)
RETURNS VOID AS
        $$
            BEGIN
                 BEGIN
                    INSERT INTO sometable3 (col_a, col_a_id, col_k)
                        VALUES(a, b, now());
                 EXCEPTION WHEN unique_violation THEN
                 -- this is some random comment.
                    RETURN;
                 END;
            END;
        $$
LANGUAGE plpgsql;


-- this is some random comment.
-- this is some random comment.
CREATE OR REPLACE FUNCTION merge_sometable(
       a sometype, -- this is some random comment.
       b CHARACTER VARYING, -- this is some random comment.
       c CHARACTER VARYING, -- this is some random comment.
       d BOOLEAN, -- this is some random comment.
       e BOOLEAN, -- this is some random comment.
       f TIMESTAMP WITH TIME ZONE, -- this is some random comment.
       g CHARACTER VARYING, -- this is some random comment.
       h CHARACTER VARYING, -- this is some random comment.
       i CHARACTER VARYING, -- this is some random comment.
       j CHARACTER VARYING, -- this is some random comment.
       k foo_col_i, -- this is some random comment.
       l CHARACTER VARYING -- this is some random comment.
)
RETURNS VOID AS
        $$
            BEGIN
                LOOP
                -- this is some random comment.
                    UPDATE sometable
                        SET
                        col_a=a,
                        col_a_id=b,
                        col_a_col_l=c,
                        col_b=d,
                        col_c=e,
                        col_d=f,
                        col_e=g,
                        col_f=h,
                        col_g=i,
                        col_h=j,
                        col_i=k,
                        extended_col_i=l
                    WHERE col_a=a AND col_a_id=b;
                    IF found THEN
                        RETURN;
                    END IF;
 -- this is some random comment.
 -- this is some random comment.
 -- this is some random comment.
                    BEGIN
                        INSERT INTO sometable (col_a, col_a_id, col_a_col_l,
                                               col_b, col_c, col_d, col_e,
                                               col_f, col_g, col_h, col_i,
                                               extended_col_i)
                        VALUES (a, b, c, d, e, f, g, h, i, j, k, l);
                        RETURN;
                    EXCEPTION WHEN unique_violation THEN
 -- this is some random comment.
                    END;
                END LOOP;
            END;
        $$
LANGUAGE plpgsql;
 
