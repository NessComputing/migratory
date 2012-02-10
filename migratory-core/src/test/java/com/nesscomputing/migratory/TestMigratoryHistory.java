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
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.support.TestClasspathLocator;

public class TestMigratoryHistory extends AbstractMigratoryTest
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
        final Map<String, List<MetadataInfo>> history = migratory.dbHistory(null);

        Assert.assertNotNull(history);
        Assert.assertEquals(2, history.size());

        List<MetadataInfo> p1Info = history.get("p1");
        Assert.assertNotNull(p1Info);
        Assert.assertEquals(1, p1Info.size());
        Assert.assertEquals(0, p1Info.get(0).getStartVersion());
        Assert.assertEquals(1, p1Info.get(0).getEndVersion());

        List<MetadataInfo> p2Info = history.get("p2");
        Assert.assertNotNull(p2Info);
        Assert.assertEquals(2, p2Info.size());
        Assert.assertEquals(0, p2Info.get(0).getStartVersion());
        Assert.assertEquals(1, p2Info.get(0).getEndVersion());
        Assert.assertEquals(1, p2Info.get(1).getStartVersion());
        Assert.assertEquals(2, p2Info.get(1).getEndVersion());
    }

    @Test
    public void testOne()
    {
        final Map<String, List<MetadataInfo>> history = migratory.dbHistory(Collections.singletonList("p1"));

        Assert.assertNotNull(history);
        // And the metadata personality
        Assert.assertEquals(1, history.size());

        List<MetadataInfo> p1Info = history.get("p1");
        Assert.assertNotNull(p1Info);
        Assert.assertEquals(1, p1Info.size());
        Assert.assertEquals(0, p1Info.get(0).getStartVersion());
        Assert.assertEquals(1, p1Info.get(0).getEndVersion());
    }

    @Test
    public void testTwo()
    {
        final Map<String, List<MetadataInfo>> history = migratory.dbHistory(ImmutableList.of("p1", "p2"));

        Assert.assertNotNull(history);
        // And the metadata personality
        Assert.assertEquals(2, history.size());

        List<MetadataInfo> p1Info = history.get("p1");
        Assert.assertNotNull(p1Info);
        Assert.assertEquals(1, p1Info.size());
        Assert.assertEquals(0, p1Info.get(0).getStartVersion());
        Assert.assertEquals(1, p1Info.get(0).getEndVersion());

        List<MetadataInfo> p2Info = history.get("p2");
        Assert.assertNotNull(p2Info);
        Assert.assertEquals(2, p2Info.size());
        Assert.assertEquals(0, p2Info.get(0).getStartVersion());
        Assert.assertEquals(1, p2Info.get(0).getEndVersion());
        Assert.assertEquals(1, p2Info.get(1).getStartVersion());
        Assert.assertEquals(2, p2Info.get(1).getEndVersion());
    }

    @Test
    public void testUnknown()
    {
        final Map<String, List<MetadataInfo>> history = migratory.dbHistory(Collections.singletonList("p3"));

        Assert.assertNotNull(history);
        // And the metadata personality
        Assert.assertEquals(0, history.size());
    }
}

