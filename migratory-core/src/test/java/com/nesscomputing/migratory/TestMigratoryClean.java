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


import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

public class TestMigratoryClean extends AbstractMigratoryTest
{
    @Override
    protected DBI getDBI() throws Exception
    {
        return new DBI(TestMigratory.H2_URL, "sa", "");
    }

    @Test
    public void testClean()
    {
        // First run must create the metatable
        Assert.assertNotNull(migratory.dbInit());

        migratory.dbClean();

        // second run must also create the table, because clean destroyed it.
        Assert.assertNotNull(migratory.dbInit());
    }

    @Test(expected = MigratoryException.class)
    public void testReadOnly()
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
            migratory.dbClean();
        }
        catch (MigratoryException me) {
            Assert.assertSame(me.getReason(), Reason.IS_READONLY);
            throw me;
        }
    }
}

