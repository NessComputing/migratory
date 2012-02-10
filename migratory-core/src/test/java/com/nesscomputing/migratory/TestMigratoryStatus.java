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
package com.nesscomputing.migratory;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.google.common.collect.ImmutableList;
import com.nesscomputing.migratory.StatusResult;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.support.TestClasspathLocator;

public class TestMigratoryStatus extends AbstractMigratoryTest
{
    @Override
    protected DBI getDBI() throws Exception
    {
        return new DBI(TestMigratory.H2_URL, "sa", "");
    }

    protected void init()
    {
        migratory.addLocator(new TestClasspathLocator(migratory, "basic-test"));
    }

    @Before
    public void setUp() throws Exception
    {
        final MigrationPlan planToExecute = new MigrationPlan()
        .addMigration("p1")
        .addMigration("p2");

        // Migrate up.
        final Map<String, List<MetadataInfo>> results = migratory.dbMigrate(planToExecute);

        Assert.assertNotNull(results);
        Assert.assertEquals(planToExecute.size(), results.size());
    }

    @Test
    public void testSimple()
    {
        final Map<String, StatusResult> status = migratory.dbStatus(null);

        Assert.assertNotNull(status);
        Assert.assertEquals(2, status.size());

        StatusResult p1Info = status.get("p1");
        Assert.assertEquals(1, p1Info.getCurrentVersion());

        StatusResult p2Info = status.get("p2");
        Assert.assertEquals(2, p2Info.getCurrentVersion());
    }

    @Test
    public void testOne()
    {
        final Map<String, StatusResult> status = migratory.dbStatus(Collections.singletonList("p1"));

        Assert.assertNotNull(status);
        // And the metadata personality
        Assert.assertEquals(1, status.size());

        StatusResult p1Info = status.get("p1");
        Assert.assertEquals(1, p1Info.getCurrentVersion());
    }

    @Test
    public void testTwo()
    {
        final Map<String, StatusResult> status = migratory.dbStatus(ImmutableList.of("p1", "p2"));

        Assert.assertNotNull(status);
        // And the metadata personality
        Assert.assertEquals(2, status.size());

        StatusResult p1Info = status.get("p1");
        Assert.assertEquals(1, p1Info.getCurrentVersion());

        StatusResult p2Info = status.get("p2");
        Assert.assertEquals(2, p2Info.getCurrentVersion());
    }
}

