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


import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;

import com.nesscomputing.migratory.TestMigratory;


/**
 * Test to demonstrate the migration functionality using H2.
 */
public class TestH2ConcurrentMigration extends AbstractConcurrentMigrationTestCase
{
    @Override
    protected IDBI getDBI()
    {
        return new DBI(TestMigratory.H2_URL, "sa", "");
    }
}
