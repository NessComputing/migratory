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


import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.MigratoryException.Reason;

public class TestMigratoryInit extends AbstractMigratoryTest
{
    private static final Random RANDOM = new Random();

    @Override
    protected DBI getDBI() throws Exception
    {
        return new DBI(TestMigratory.H2_URL, "sa", "");
    }

    @Test
    public final void testInit()
    {
        // First run must create the metatable
        Assert.assertNotNull(migratory.dbInit());

        // Second run must already have the metatable
        Assert.assertNull(migratory.dbInit());
    }

    @Test
    public final void testRandom()
    {
        final long random = Math.abs(RANDOM.nextLong());

        final MigratoryConfig migratoryConfig = new MigratoryConfig() {
            public String getMetadataTableName()
            {
                return "migratory_info_" + random;
            }
        };

        migratory = new Migratory(migratoryConfig, dbi);

        // First run must create the metatable
        Assert.assertNotNull(migratory.dbInit());

        Assert.assertNull(migratory.dbInit());
    }

    @Test(expected = MigratoryException.class)
    public final void testReadOnly()
    {
        final MigratoryConfig migratoryConfig = new MigratoryConfig() {
            @Override
            public boolean isReadOnly()
            {
                return true;
            }
        };

        migratory = new Migratory(migratoryConfig, dbi);

        try {
            migratory.dbInit();
        }
        catch (MigratoryException me) {
            Assert.assertSame(me.getReason(), Reason.IS_READONLY);
            throw me;
        }
    }

}
