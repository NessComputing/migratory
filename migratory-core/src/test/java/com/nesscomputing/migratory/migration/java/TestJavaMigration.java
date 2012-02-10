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
package com.nesscomputing.migratory.migration.java;


import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;

import com.nesscomputing.migratory.AbstractMigratoryTest;
import com.nesscomputing.migratory.TestMigratory;
import com.nesscomputing.migratory.locator.MigrationLocator;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.Migration;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationType;
import com.nesscomputing.migratory.support.TestClasspathLocator;

public class TestJavaMigration  extends AbstractMigratoryTest
{
    public static final String PERSONALITY_NAME = "test";

    private MyMigration migration = null;

    @After
    public void tearDown()
    {
        Assert.assertNotNull(migration);
        Assert.assertTrue(migration.isTriggered());
    }


    @Override
    protected IDBI getDBI()
    {
        return new DBI(TestMigratory.H2_URL, "sa", "");
    }

    protected String getTestName()
    {
        return "migration";
    }

    @Override
    protected void init()
    {
        this.migration = new MyMigration();
        migratory.addLocator(new TestClasspathLocator(migratory, getTestName()));
        migratory.addLocator(new JavaLocator(this.migration));
    }

    @Test
    public void testMigrate() throws Exception
    {
        migrateAndCheckResult(migratory, new MigrationPlan(PERSONALITY_NAME), PERSONALITY_NAME, 1, 4, 4);

        Map<String, List<MetadataInfo>> history = migratory.dbHistory(null);
        List<MetadataInfo> testHistory = history.get(PERSONALITY_NAME);

        Assert.assertNotNull(testHistory);
        Assert.assertEquals(4, testHistory.size());

        Assert.assertEquals(MigrationType.SQL, testHistory.get(0).getType());
        Assert.assertEquals(MigrationType.SQL, testHistory.get(1).getType());
        Assert.assertEquals(MigrationType.SQL, testHistory.get(2).getType());
        Assert.assertEquals(MigrationType.JAVA, testHistory.get(3).getType());
    }

    private static class JavaLocator implements MigrationLocator
    {
        private final Migration migration;

        public JavaLocator(final Migration migration)
        {
            this.migration = migration;
        }

        @Override
        public void contributeMigrations(Map<String, Migration> migrations, String personalityName, String databaseType)
        {
            if("test".equals(personalityName)) {
                migrations.put(migration.getLocation(), migration);
            }
        }

        @Override
        public boolean isSystemLoader()
        {
            return false;
        }
    }

    public static class MyMigration extends Migration
    {
        private boolean triggered = false;

        public MyMigration()
        {
            super(MigrationType.JAVA, "test", "testScript");
        }

        @Override
        public int getStartVersion()
        {
            return 3;
        }

        @Override
        public int getEndVersion()
        {
            return 4;
        }

        @Override
        public String getDescription()
        {
            return "A generic java migration";
        }

        @Override
        public String getLocation()
        {
            return this.getClass().getName();
        }

        @Override
        public String getChecksum()
        {
            return (this.getClass().getName() + ":" + this.hashCode()).substring(0, 39);
        }

        @Override
        public void migrate(Handle handle)
        {
            triggered = true;
        }

        @Override
        public boolean isNeedsRoot()
        {
            return false;
        }

        public boolean isTriggered()
        {
            return triggered;
        }
    }
}

