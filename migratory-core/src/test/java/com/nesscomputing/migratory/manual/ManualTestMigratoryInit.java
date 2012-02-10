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


import org.junit.After;
import org.junit.Assert;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.TestMigratoryInit;

public class ManualTestMigratoryInit extends TestMigratoryInit
{
    protected DBI getDBI()
    {
        return new DBI("jdbc:postgresql://localhost/migratory_test", "postgres", "");
    }

    @After
    public void shutdownDb()
    {
        Assert.assertNotNull(dbi);

        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        final Migratory migratory = new Migratory(migratoryConfig, dbi);
        migratory.dbClean();
    }
}

