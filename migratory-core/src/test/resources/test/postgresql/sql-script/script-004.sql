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

CREATE TABLE sometable(
	col_a	BIGINT NOT NULL,
	col_b		sometype NOT NULL,
	col_b_id	CHARACTER VARYING(255) NOT NULL,

	col_c	TIMESTAMP WITH TIME ZONE NOT NULL,
	col_b_uri	CHARACTER VARYING(1024),

	col_d	CHARACTER VARYING(128),
	col_e	CHARACTER VARYING(128),
	col_f	CHARACTER VARYING(255) NOT NULL,
	col_g	CHARACTER VARYING(512) NOT NULL,
	col_h		CHARACTER VARYING(16),

	CONSTRAINT pk_sometable PRIMARY KEY(col_b, col_b_id)
);

CREATE SEQUENCE sometable_seq START 1000;

CREATE INDEX sometable_col_a_idx ON sometable(col_a);

CREATE OR REPLACE FUNCTION merge_sometable (
	a	BIGINT,				-- some random comment
	b	sometype,			-- some random comment
	c	CHARACTER VARYING(255),		-- some random comment

	d	TIMESTAMP WITH TIME ZONE,	-- some random comment
	e	CHARACTER VARYING(1024),	-- some random comment

	f	CHARACTER VARYING(128),		-- some random comment
	g	CHARACTER VARYING(128),		-- some random comment
	h	CHARACTER VARYING(255),		-- some random comment
	i	CHARACTER VARYING(512),		-- some random comment
	j	CHARACTER VARYING(16))		-- some random comment
RETURNS BIGINT AS
$$
DECLARE
	existing_id BIGINT;
BEGIN
	LOOP
		-- some random comment
		UPDATE sometable
		SET
			col_c=d,
			col_b_uri=e,
			col_d=f,
			col_e=g,
			col_f=h,
			col_g=i,
			col_h=j
		WHERE col_a=a AND col_b=b AND col_b_id=c;
		IF found THEN
			RETURN a;
		END IF;
		-- some random comment
		-- some random comment
		-- some random comment
		SELECT col_a INTO existing_id
		FROM sometable WHERE col_b=b AND col_b_id=c;
		IF found THEN
			UPDATE sometable
			SET	
				col_c=d,
				col_b_uri=e,
				col_d=f,
				col_e=g,
				col_f=h,
				col_g=i,
				col_h=j
			WHERE col_a=existing_id AND col_b=b AND col_b_id=c;
			IF found THEN
				RETURN existing_id;
			END IF;
		END IF;

		-- some random comment
		-- some random comment
		-- some random comment
		BEGIN
			INSERT INTO sometable (col_a, col_b, col_b_id,
				col_c, col_b_uri,
				col_d, col_e, col_f,
				col_g, col_h)
			VALUES (a, b, c, d, e, f, g, h, i, j);
			RETURN a;
		EXCEPTION WHEN unique_violation THEN
			-- some random comment
		END;
	END LOOP;
END;
$$
LANGUAGE plpgsql;


INSERT INTO sometable(col_a, col_b, col_b_id, col_c, col_d, col_e, col_f, col_g)
	VALUES(0, 'TRUMPET', '0', NOW(), 'VALUE_1', 'VALUE_2', 'VALUE_3', 'VALUE_4');

-- some random comment
ALTER TABLE sometable ADD CONSTRAINT sometable_id_correspondence CHECK
(col_b <> 'TRUMPET'::sometype OR col_b_id = col_a::text);


