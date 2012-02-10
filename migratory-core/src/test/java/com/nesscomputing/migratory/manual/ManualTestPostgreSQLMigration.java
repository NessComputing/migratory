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
package com.nesscomputing.migratory.manual;


import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.migration.AbstractMigrationTestCase;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.support.TestClasspathLocator;


/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
public class ManualTestPostgreSQLMigration extends AbstractMigrationTestCase
{
    @Override
    protected IDBI getDBI()
    {
        return new DBI("jdbc:postgresql://localhost/migratory_test", "postgres", "");
    }

    @Override
    protected String getTestName()
    {
        return "migration";
    }

    @Test
    public void storedProcedure() throws Exception
    {
        migratory = new Migratory(new MigratoryConfig() {}, dbi);
        init();
        migratory.addLocator(new TestClasspathLocator(migratory, "postgresql/procedure"));
        migratory.dbMigrate(new MigrationPlan("procedure"));

        Assert.assertEquals("Hello", queryForString("SELECT value FROM test_data"));

        migratory.dbClean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        migratory.dbMigrate(new MigrationPlan("procedure"));
    }

    @Test
    public void trigger() throws Exception
    {
        migratory = new Migratory(new MigratoryConfig() {}, dbi);
        init();
        migratory.addLocator(new TestClasspathLocator(migratory, "postgresql/trigger"));
        migratory.dbMigrate(new MigrationPlan("trigger"));

        Assert.assertEquals(10, queryForInt("SELECT count(*) FROM test4"));

        migratory.dbClean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        migratory.dbMigrate(new MigrationPlan("trigger"));

    }

    @Test
    public void view() throws Exception
    {
        migratory = new Migratory(new MigratoryConfig() {}, dbi);
        init();
        migratory.addLocator(new TestClasspathLocator(migratory, "postgresql/view"));
        migratory.dbMigrate(new MigrationPlan("view"));

        Assert.assertEquals(150, queryForInt("SELECT value FROM v"));

        migratory.dbClean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        migratory.dbMigrate(new MigrationPlan("view"));
    }
}
