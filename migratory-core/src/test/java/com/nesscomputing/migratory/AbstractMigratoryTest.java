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


import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.IntegerMapper;
import org.skife.jdbi.v2.util.StringMapper;

import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.migration.MigrationType;
import com.nesscomputing.migratory.migration.sql.SqlMigration;

public abstract class AbstractMigratoryTest
{
    protected IDBI dbi = null;
    protected Migratory migratory = null;

    protected abstract IDBI getDBI() throws Exception;

    @Before
    public final void prepare() throws Exception
    {
        dbi = getDBI();
        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        migratory = new Migratory(migratoryConfig, dbi);
        init();

        migratory.dbClean();
    }

    @After
    public final void erase()
    {
        Assert.assertNotNull(migratory);

        if (!migratory.getConfig().isReadOnly()) {
            migratory.dbClean();
        }
        migratory = null;

        Assert.assertNotNull(dbi);
        dbi = null;
    }

    protected void init()
    {
    }

    protected String queryForString(final String query)
    {
        return dbi.withHandle(new HandleCallback<String>() {
                @Override
                public String withHandle(final Handle handle) throws SQLException {
                    return handle.createQuery(query)
                        .map(StringMapper.FIRST)
                        .first();
                }
            });
    }

    protected int queryForInt(final String query)
    {
        return dbi.withHandle(new HandleCallback<Integer>() {
                @Override
                public Integer withHandle(final Handle handle) throws SQLException {
                    return handle.createQuery(query)
                        .map(IntegerMapper.FIRST)
                        .first();
                }
            });
    }

    protected boolean tableExists(final String tableName)
    {
        return migratory.getDbSupport().tableExists(tableName);
    }

    protected boolean columnExists(final String tableName, final String columnName)
    {
        return migratory.getDbSupport().columnExists(tableName, columnName);
    }

    protected void assertChecksum(MigratoryContext context, MetadataInfo info, final String personalityName) throws Exception
    {
        if (info.getType() == MigrationType.SQL) {
            final URI location = URI.create(info.getLocation());
            final String rawSql = Resources.toString(location.toURL(), Charsets.UTF_8);
            final SqlMigration migration = new SqlMigration(context, personalityName, URI.create(info.getLocation()), info.getScriptName(), rawSql);
            Assert.assertEquals(migration.getChecksum(), info.getChecksum());
        }
    }

    protected void migrateAndCheckResult(final MigratoryContext context, MigrationPlan migrationPlan, String personalityName, int resultSize, int steps, int lastMigration) throws Exception
    {
        Map<String, List<MetadataInfo>> result = migratory.dbMigrate(migrationPlan);

        Assert.assertEquals(resultSize, result.size());
        if (result.size() > 0) {
            List<MetadataInfo> testResult = result.get(personalityName);
            Assert.assertNotNull(testResult);
            Assert.assertEquals(steps, testResult.size());
        }

        final Map<String, StatusResult> status = migratory.dbStatus(null);
        final StatusResult testStatus = status.get(personalityName);

        Assert.assertNotNull(testStatus);
        Assert.assertTrue(testStatus.isMigrationPossible());
        Assert.assertEquals(lastMigration == testStatus.getLastVersion() ? MigrationDirection.DO_NOTHING : MigrationDirection.FORWARD, testStatus.getDirection());
        Assert.assertEquals(lastMigration, testStatus.getCurrentVersion());
        Assert.assertEquals(MigrationState.OK, testStatus.getLastState());

        Map<String, List<MetadataInfo>> history = migratory.dbHistory(null);
        List<MetadataInfo> testHistory = history.get(personalityName);

        Assert.assertNotNull(testHistory);
        Assert.assertEquals(lastMigration, testHistory.size());

        for (MetadataInfo info : testHistory) {
            assertChecksum(context, info, personalityName);
        }
    }


}
