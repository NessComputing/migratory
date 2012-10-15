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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.dbsupport.DbSupport;
import com.nesscomputing.migratory.dbsupport.h2.H2DbSupport;

public class TestMigratory
{
    public static final String H2_URL = "jdbc:h2:mem:migratory_test;DB_CLOSE_DELAY=-1";

    @Test
    public void testSimple()
    {
        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        final DBI dbi = new DBI(H2_URL, "sa", "");

        final Migratory migratory = new Migratory(migratoryConfig, dbi);

        Assert.assertThat(migratoryConfig, is(sameInstance(migratory.getConfig())));

        final DbSupport dbSupport = migratory.getDbSupport();
        Assert.assertNotNull(dbSupport);
        Assert.assertSame(dbSupport.getClass(), H2DbSupport.class);
    }

    @Test
    public void testWithConfig()
    {
        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        final MigratoryDBIConfig dbiConfig = new MigratoryDBIConfig() {
            @Override
            public String getDBUrl()
            {
                return H2_URL;
            }

            @Override
            public String getDBUser()
            {
                return "sa";
            }

            @Override
            public String getDBPassword()
            {
                return "";
            }
        };
        final Migratory migratory = new Migratory(migratoryConfig, dbiConfig);

        Assert.assertThat(migratoryConfig, is(sameInstance(migratory.getConfig())));

        final DbSupport dbSupport = migratory.getDbSupport();
        Assert.assertNotNull(dbSupport);
        Assert.assertSame(dbSupport.getClass(), H2DbSupport.class);
    }

    @Test
    public void testWithProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty("migratory.url", H2_URL);
        properties.setProperty("migratory.user", "sa");
        properties.setProperty("migratory.password", "");

        properties.setProperty("migratory.readonly", "true");
        properties.setProperty("migratory.create_personalities", "false");
        properties.setProperty("migratory.allow_rollforward", "false");
        properties.setProperty("migratory.allow_rollback", "true");
        properties.setProperty("migratory.metadata_table", "foobar");

        final Migratory migratory = new Migratory(properties);

        final DbSupport dbSupport = migratory.getDbSupport();
        Assert.assertNotNull(dbSupport);
        Assert.assertSame(dbSupport.getClass(), H2DbSupport.class);

        final MigratoryConfig config = migratory.getConfig();
        Assert.assertEquals("foobar", config.getMetadataTableName());
        Assert.assertTrue(config.isReadOnly());
        Assert.assertFalse(config.isCreatePersonalities());
        Assert.assertFalse(config.isAllowRollForward());
        Assert.assertTrue(config.isAllowRollBack());
    }

}



