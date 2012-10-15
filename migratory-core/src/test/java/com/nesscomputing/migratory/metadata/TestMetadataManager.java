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
package com.nesscomputing.migratory.metadata;


import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.support.TestClasspathLocator;

public class TestMetadataManager
{
    private DBI dbi = null;

    protected Migratory migratory = null;

    protected DBI getDBI()
    {
        return new DBI("jdbc:h2:mem:migratory_test;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Before
    public void setUp()
    {
        dbi = getDBI();

        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        migratory = new Migratory(migratoryConfig, dbi);
        migratory.addLocator(new TestClasspathLocator(migratory, "basic-test"));
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(dbi);
        Assert.assertNotNull(migratory);
        dbi = null;
        migratory = null;
    }

    private void init()
    {

        final MigrationPlan migrations = new MigrationPlan("p1", "p2");

        // Migrate up.
        final Map<String, List<MetadataInfo>> results = migratory.dbMigrate(migrations);

        Assert.assertNotNull(results);
        Assert.assertEquals(migrations.size(), results.size());
    }

    @Test
    public void testSimple()
    {
        init();

        final MetadataManager manager = new MetadataManager(migratory);

        final Map<String, Integer> versions = manager.retrieveCurrentVersions();

        Assert.assertNotNull(versions);
        Assert.assertEquals(2, versions.size());

        Integer p1Info = versions.get("p1");
        Assert.assertNotNull(p1Info);
        Assert.assertEquals(Integer.valueOf(1), p1Info);

        Integer p2Info = versions.get("p2");
        Assert.assertNotNull(p2Info);
        Assert.assertEquals(Integer.valueOf(2), p2Info);
    }
}

