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


import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.time.StopWatch;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;


public class DbMigrator
{
    private static final Log LOG = Log.findLog();

    private final MigratoryContext migratoryContext;
    private final MigrationPlanner migrationPlanner;

    public DbMigrator(final MigratoryContext migratoryContext, final MigrationPlanner migrationPlanner)
    {
        this.migratoryContext = migratoryContext;
        this.migrationPlanner = migrationPlanner;
    }

    public  List<MigrationResult> migrate(final MigratoryOption [] options)
    {
        final List<MigrationResult> migrationResults = Lists.newArrayList();

        for(final Migration migration : migrationPlanner.getPlannedMigrations()) {
            LOG.debug("Executing '%s'", migration);

            MigrationState migrationState = null;

            final StopWatch stopWatch = new StopWatch();

            stopWatch.start();
            try {
                final IDBI dbi = migration.isNeedsRoot() ? migratoryContext.getRootDBI() : migratoryContext.getDBI();

                if (!MigratoryOption.containsOption(MigratoryOption.DRY_RUN, options)) {
                    // Metadata migrations are always run
                    if (!MigratoryOption.containsOption(MigratoryOption.METADATA_ONLY, options) || migration.getPersonalityName().equals(MetadataManager.METADATA_MIGRATION_NAME)) {
                        dbi.inTransaction(new TransactionCallback<Void>() {
                            @Override
                            public Void inTransaction(final Handle migrationHandle, final TransactionStatus status) {

                                migrationHandle.define("migration_personality_name", migration.getPersonalityName());
                                migrationHandle.define("migration_strategy", migrationPlanner.getDirection());
                                migrationHandle.define("migration_type", migration.getType());
                                migrationHandle.define("migration_start_version", migration.getStartVersion());
                                migrationHandle.define("migration_end_version", migration.getEndVersion());

                                migration.migrate(migrationHandle);
                                return null;

                            }
                        });
                    }
                    else {
                        LOG.trace("No metadata migration!");
                    }
                }
                migrationState = MigrationState.OK;
            }
            catch (Exception e) {
                migrationState = MigrationState.FAIL;
                LOG.warnDebug(e, "Migration failed:");
            }
            finally {
                stopWatch.stop();
            }

            final long executionTime = stopWatch.getTime();
            if (!MigratoryOption.containsOption(MigratoryOption.DRY_RUN, options)) {
                migrationResults.add(new MigrationResult(migrationState, migrationPlanner.getDirection(), executionTime, migration));
            }

            if (migrationState != MigrationState.OK) {
                break; // Don't do any further migrations
            }
        }
        return migrationResults;
    }
}
