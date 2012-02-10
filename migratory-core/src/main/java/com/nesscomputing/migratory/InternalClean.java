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


import java.sql.SQLException;

import org.apache.commons.lang3.time.StopWatch;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.migration.sql.SqlScript;
import com.nesscomputing.migratory.migration.sql.SqlStatement;

class InternalClean extends AbstractMigratorySupport
{
    private static final Logger LOG = LoggerFactory.getLogger(InternalClean.class);

    String schema = null;

    private final MigratoryContext migratoryContext;


    InternalClean(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
    }

    void clean(final MigratoryOption [] options)
    {
        LOG.debug("Running clean()");

        if (migratoryContext.getConfig().isReadOnly()) {
            throw new MigratoryException(Reason.IS_READONLY);
        }

        LOG.debug("Starting to drop all database objects ...");
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            migratoryContext.getRootDBI().withHandle(new HandleCallback<Integer>() {
                @Override
                public Integer withHandle(final Handle handle) throws SQLException {

                    final SqlScript cleanScript = migratoryContext.getDbSupport().createCleanScript(handle);
                    setSchema(migratoryContext.getDbSupport().getCurrentSchema(handle));

                    for (SqlStatement sqlStatement : cleanScript.getSqlStatements()) {
                        try {

                            if (!MigratoryOption.containsOption(MigratoryOption.DRY_RUN, options)) {
                                handle.createStatement(sqlStatement.getSql())
                                .define(sqlStatement.getDefines())
                                .execute();
                            }
                        }
                        catch (DBIException e) {
                            LOG.warn("While executing {} : {}", sqlStatement, e.getMessage());
                        }
                    }
                    return 0;
                }
            });
        }
        catch (Exception e) {
            throw processException(e);
        }
        finally {
            stopWatch.stop();
        }

        LOG.info(String.format("Cleaned database schema '%s' (execution time %s ms)", schema, stopWatch.getTime()));
    }

    protected void setSchema(final String schema)
    {
        this.schema = schema;
    }
}
