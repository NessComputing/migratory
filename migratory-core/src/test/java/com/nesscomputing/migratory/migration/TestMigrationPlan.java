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
package com.nesscomputing.migratory.migration;


import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationPlan.MigrationPlanEntry;

public class TestMigrationPlan
{
    @Test
    public void testHigherWins()
    {
        MigrationPlan migrationPlan = new MigrationPlan();
        migrationPlan.addMigration("world", Integer.MAX_VALUE);
        migrationPlan.addMigration("hello", Integer.MAX_VALUE, 1);

        final Iterator<MigrationPlanEntry> it = migrationPlan.getEntries().iterator();
        Assert.assertEquals("hello", it.next().getPersonalityName());
        Assert.assertEquals("world", it.next().getPersonalityName());
    }

    @Test
    public void testLowerLoses()
    {
        MigrationPlan migrationPlan = new MigrationPlan();
        migrationPlan.addMigration("world", Integer.MAX_VALUE);
        migrationPlan.addMigration("hello", Integer.MAX_VALUE, -1);

        final Iterator<MigrationPlanEntry> it = migrationPlan.getEntries().iterator();
        Assert.assertEquals("world", it.next().getPersonalityName());
        Assert.assertEquals("hello", it.next().getPersonalityName());
    }
}


