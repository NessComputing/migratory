/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.migratory.dbsupport;


import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.nesscomputing.migratory.dbsupport.postgresql.PostgreSQLSqlScript;
import com.nesscomputing.migratory.migration.sql.SqlScript;
import com.nesscomputing.migratory.migration.sql.SqlStatement;

public class TestPostgreSQLScript
{
    private void runTest(final String [] testStatements, final String scriptFile) throws Exception
    {
        final String contents = Resources.toString(Resources.getResource(this.getClass(), scriptFile), Charsets.UTF_8);

        final SqlScript script = new PostgreSQLSqlScript(contents);

        final Collection<SqlStatement> statements = script.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(testStatements.length, statements.size());

        for (SqlStatement statement : statements) {
            Assert.assertEquals(testStatements[statement.getCount()], statement.getSql());
        }
    }

    @Test
    public void testFile1() throws Exception
    {
        final String [] testStatements = new String [] {
                        "LOCK sometable",
                        "ALTER TABLE sometable ADD last_modified TIMESTAMP",
                        "ALTER TABLE sometable ALTER COLUMN last_modified SET DEFAULT CURRENT_TIMESTAMP",
                        "UPDATE sometable SET last_modified=CURRENT_TIMESTAMP",
                        "CREATE OR REPLACE FUNCTION update_lastmodified_column() RETURNS TRIGGER AS ' BEGIN NEW.last_modified = NOW(); RETURN NEW; END; ' LANGUAGE 'plpgsql'",
                        "CREATE TRIGGER update_lastmodified_modtime BEFORE UPDATE ON sometable FOR EACH ROW EXECUTE PROCEDURE update_lastmodified_column()"
        };

        runTest(testStatements, "/test/postgresql/sql-script/script-001.sql");
    }

    @Test
    public void testFile2() throws Exception
    {
        final String [] testStatements = new String [] {
            "CREATE OR REPLACE FUNCTION somefunction() RETURNS VOID AS ' DECLARE "
            + "szgid RECORD; BEGIN FOR szgid IN SELECT sz, group_id, count(*) FROM "
            + "sometable GROUP BY sz, group_id HAVING count(*) > 1 LOOP "
            + "DELETE FROM sometable WHERE sz = szgid.sz AND group_id = szgid.group_id; "
            + "INSERT INTO sometable (sz, group_id) VALUES (szgid.sz, szgid.group_id); "
            + "END LOOP; END' LANGUAGE 'plpgsql'"
        };

        runTest(testStatements, "/test/postgresql/sql-script/script-002.sql");
    }

    @Test
    public void testFile3() throws Exception
    {
        final String [] testStatements = new String [] {
            "CREATE TABLE sometable ( col_a sometype NOT NULL, col_a_id CHARACTER VARYING NOT NULL, "
            + "col_a_col_l CHARACTER VARYING NOT NULL, col_b BOOLEAN NOT NULL, col_c BOOLEAN NOT NULL, "
            + "col_d TIMESTAMP WITH TIME ZONE NOT NULL, col_e CHARACTER VARYING (256), col_f CHARACTER "
            + "VARYING (1024), col_g CHARACTER VARYING (256), col_h CHARACTER VARYING NOT NULL, col_i "
            + "foo_col_i NOT NULL, extended_col_i CHARACTER VARYING, PRIMARY KEY (col_a, col_a_id) )",
            "CREATE INDEX sometable_col_d_idx on sometable(col_d)",
            "CREATE TABLE sometable2 ( col_a sometype NOT NULL, col_a_place_id CHARACTER VARYING NOT NULL, "
            + "col_h_col_a sometype, col_h_id CHARACTER VARYING, col_j BIGINT, col_d TIMESTAMP WITH TIME "
            + "ZONE NOT NULL, col_l BIGINT NOT NULL )",
            "CREATE INDEX sometable2_place_id_idx ON sometable2 (col_j) WHERE col_j IS NOT NULL",
            "CREATE TABLE sometable3 ( col_a sometype NOT NULL, col_a_id CHARACTER VARYING NOT NULL, col_k "
            + "TIMESTAMP WITH TIME ZONE NOT NULL, CONSTRAINT pk_sometable3 PRIMARY KEY (col_a, col_a_id) )",
            "CREATE TABLE sometable4 ( id serial, col_m bigint NOT NULL, col_h_col_a sometype NOT NULL, "
            + "col_h_col_a_id character varying NOT NULL, CONSTRAINT pk_sometable4 PRIMARY KEY (col_m, col_h_col_a, "
            + "col_h_col_a_id) )",
            "CREATE INDEX sometable4_m_idx ON sometable4 (col_m)",
            "CREATE INDEX sometable4_p_idx ON sometable4 (col_h_col_a, col_h_col_a_id)",
            "ALTER TABLE sometable4 ADD CONSTRAINT fk_sometable5 FOREIGN KEY (col_h_col_a, col_h_col_a_id) "
            + "REFERENCES sometable (col_a, col_a_id)",
            "CREATE OR REPLACE FUNCTION merge_sometable3( a sometype, b CHARACTER VARYING ) RETURNS VOID AS $$ "
            + "BEGIN BEGIN INSERT INTO sometable3 (col_a, col_a_id, col_k) VALUES(a, b, now()); EXCEPTION WHEN "
            + "unique_violation THEN RETURN; END; END; $$ LANGUAGE plpgsql",
            "CREATE OR REPLACE FUNCTION merge_sometable( a sometype, b CHARACTER VARYING, c CHARACTER VARYING, "
            + "d BOOLEAN, e BOOLEAN, f TIMESTAMP WITH TIME ZONE, g CHARACTER VARYING, h CHARACTER VARYING, i "
            + "CHARACTER VARYING, j CHARACTER VARYING, k foo_col_i, l CHARACTER VARYING ) RETURNS VOID AS $$ "
            + "BEGIN LOOP UPDATE sometable SET col_a=a, col_a_id=b, col_a_col_l=c, col_b=d, col_c=e, col_d=f, "
            + "col_e=g, col_f=h, col_g=i, col_h=j, col_i=k, extended_col_i=l WHERE col_a=a AND col_a_id=b; IF "
            + "found THEN RETURN; END IF; BEGIN INSERT INTO sometable (col_a, col_a_id, col_a_col_l, col_b, col_c, "
            + "col_d, col_e, col_f, col_g, col_h, col_i, extended_col_i) VALUES (a, b, c, d, e, f, g, h, i, j, k, l); "
            + "RETURN; EXCEPTION WHEN unique_violation THEN END; END LOOP; END; $$ LANGUAGE plpgsql"
        };

        runTest(testStatements, "/test/postgresql/sql-script/script-003.sql");
    }

    @Test
    public void testFile4() throws Exception
    {
        final String [] testStatements = new String [] {
            "CREATE TABLE sometable( col_a	BIGINT NOT NULL, col_b		sometype NOT NULL, col_b_id	CHARACTER VARYING(255) NOT NULL, col_c	"
            + "TIMESTAMP WITH TIME ZONE NOT NULL, col_b_uri	CHARACTER VARYING(1024), col_d	CHARACTER VARYING(128), col_e	CHARACTER "
            + "VARYING(128), col_f	CHARACTER VARYING(255) NOT NULL, col_g	CHARACTER VARYING(512) NOT NULL, col_h		CHARACTER "
            + "VARYING(16), CONSTRAINT pk_sometable PRIMARY KEY(col_b, col_b_id) )",
            "CREATE SEQUENCE sometable_seq START 1000",
            "CREATE INDEX sometable_col_a_idx ON sometable(col_a)",
            "CREATE OR REPLACE FUNCTION merge_sometable ( a	BIGINT, b	sometype, c	CHARACTER VARYING(255), d	TIMESTAMP WITH TIME ZONE, "
            + "e	CHARACTER VARYING(1024), f	CHARACTER VARYING(128), g	CHARACTER VARYING(128), h	CHARACTER VARYING(255), i	"
            + "CHARACTER VARYING(512), j	CHARACTER VARYING(16)) RETURNS BIGINT AS $$ DECLARE existing_id BIGINT; BEGIN LOOP "
            + "UPDATE sometable SET col_c=d, col_b_uri=e, col_d=f, col_e=g, col_f=h, col_g=i, col_h=j WHERE col_a=a AND col_b=b "
            + "AND col_b_id=c; IF found THEN RETURN a; END IF; SELECT col_a INTO existing_id FROM sometable WHERE col_b=b AND "
            + "col_b_id=c; IF found THEN UPDATE sometable SET col_c=d, col_b_uri=e, col_d=f, col_e=g, col_f=h, col_g=i, col_h=j "
            + "WHERE col_a=existing_id AND col_b=b AND col_b_id=c; IF found THEN RETURN existing_id; END IF; END IF; BEGIN INSERT "
            + "INTO sometable (col_a, col_b, col_b_id, col_c, col_b_uri, col_d, col_e, col_f, col_g, col_h) VALUES (a, b, c, d, e, "
            + "f, g, h, i, j); RETURN a; EXCEPTION WHEN unique_violation THEN END; END LOOP; END; $$ LANGUAGE plpgsql",
            "INSERT INTO sometable(col_a, col_b, col_b_id, col_c, col_d, col_e, col_f, col_g) VALUES(0, 'TRUMPET', '0', NOW(), "
            + "'VALUE_1', 'VALUE_2', 'VALUE_3', 'VALUE_4')",
            "ALTER TABLE sometable ADD CONSTRAINT sometable_id_correspondence CHECK (col_b <> 'TRUMPET'::sometype OR col_b_id = col_a::text)",
        };

        runTest(testStatements, "/test/postgresql/sql-script/script-004.sql");
    }

    @Test
    public void testFile5() throws Exception
    {
        final String [] testStatements = new String [] {
            "LOCK sometable",
            "CREATE SEQUENCE sometable_last_modified_seq",
            "ALTER TABLE sometable ALTER last_modified DROP DEFAULT",
            "ALTER TABLE sometable ALTER last_modified TYPE BIGINT USING 0",
            "ALTER TABLE sometable ALTER last_modified SET DEFAULT nextval('sometable_last_modified_seq')",
            "ALTER SEQUENCE sometable_last_modified_seq OWNED BY sometable.last_modified",
            "CREATE OR REPLACE FUNCTION update_lastmodified_column() RETURNS TRIGGER AS ' BEGIN NEW.last_modified = nextval(''sometable_last_modified_seq''); RETURN NEW; END; ' LANGUAGE 'plpgsql'",
            "UPDATE sometable SET id=id WHERE id=1"
        };

        runTest(testStatements, "/test/postgresql/sql-script/script-005.sql");
    }

    @Test
    public void testCFunctions() throws Exception
    {
    	Logger.getRootLogger().setLevel(Level.TRACE);
    	final String [] testStatements = new String [] {
                "SET search_path = public",
                "CREATE OR REPLACE FUNCTION uuid_nil() RETURNS uuid AS '$libdir/uuid-ossp', 'uuid_nil' IMMUTABLE STRICT LANGUAGE C",
                "CREATE OR REPLACE FUNCTION uuid_ns_dns() RETURNS uuid AS '$libdir/uuid-ossp', 'uuid_ns_dns' IMMUTABLE STRICT LANGUAGE C"
            };

            runTest(testStatements, "/test/postgresql/sql-script/c-functions.sql");
    }
}

