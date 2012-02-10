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


import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.TestMigratory;
import com.nesscomputing.migratory.dbsupport.DbSupport;
import com.nesscomputing.migratory.dbsupport.h2.H2DbSupport;

public class TestDbSupportFactory
{
    @Test
    public void testRegisterSupport()
    {
        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        final DBI dbi = new DBI(TestMigratory.H2_URL, "sa", "");

        final Migratory migratory = new H3Migratory(migratoryConfig, dbi);
        final DbSupport dbSupport = migratory.getDbSupport();

        Assert.assertSame(dbSupport.getClass(), H3DbSupport.class);
    }

    private static class H3Migratory extends Migratory
    {
        private H3Migratory(final MigratoryConfig config, final IDBI dbi)
        {
            super(config, dbi);

            addDbSupport("H2", H3DbSupport.class);
        }
    }

    private static class H3DbSupport extends H2DbSupport
    {
        public H3DbSupport(final IDBI dbi)
        {
            super(dbi);
        }
    }
}



