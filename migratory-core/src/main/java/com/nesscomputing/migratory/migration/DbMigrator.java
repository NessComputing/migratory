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

import org.apache.commons.lang3.time.StopWatch;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;


public class DbMigrator
{
    private static final Logger LOG = LoggerFactory.getLogger(DbMigrator.class);

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
            LOG.debug("Executing '{}'", migration);

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
                LOG.warn("Migration failed!", e);
            }
            finally {
                stopWatch.stop();
            }

            final long executionTime = stopWatch.getTime();
            if (!MigratoryOption.containsOption(MigratoryOption.DRY_RUN, options)) {
                migrationResults.add(new MigrationResult(migrationState, migrationPlanner.getDirection(), executionTime, migration));
            }

            if (migrationState == MigrationState.FAIL) {
                break; // Don't do any further migrations
            }
        }
        return migrationResults;
    }
}


//         if (MigrationState.FAILED.equals(state) && dbSupport.supportsDdlTransactions()) {
//             throw new MigrationException(migration.getVersion(), true);
//         }
//         LOG.debug(String.format("Finished migrating to version %s (execution time %s)",
//                 migration.getVersion(), TimeFormat.format(executionTime)));

//         metaDataTableRow.update(executionTime, state);
//         metaDataTable.insert(metaDataTableRow);

//         return metaDataTableRow;
//     }


//         }
//         return null;
//     }


// }

//     /**
//      * Logger.
//      */
//     private static final Logger LOG = LoggerFactory.getLogger(DbMigrator.class);

//     /**
//      * The target version of the migration.
//      */
//     private final SchemaVersion target;

//     /**
//      * Database-specific functionality.
//      */
//     private final DbSupport dbSupport;

//     /**
//      * The database metadata table.
//      */
//     private final MetaDataTable metaDataTable;

//     private final Handle handle;

//     private final IDBI dbi;

//     /**
//      * Flag whether to ignore failed future migrations or not.
//      */
//     private final boolean ignoreFailedFutureMigration;

//     /**
//      * Creates a new database migrator.
//      */
//     public DbMigrator(IDBI dbi, Handle handle, DbSupport dbSupport,
//                       MetaDataTable metaDataTable, SchemaVersion target, boolean ignoreFailedFutureMigration) {
//         this.dbi = dbi;
//         this.handle = handle;
//         this.dbSupport = dbSupport;
//         this.metaDataTable = metaDataTable;
//         this.target = target;
//         this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
//     }

//     /**
//      * Starts the actual migration.
//      */
//     public int migrate(final List<Migration> migrations) throws SQLException {
//         int migrationSuccessCount = 0;

//         while (true) {
//             try {
//                 // Hold the lock during all subsequent transactions.
//                 handle.begin();
//                 metaDataTable.lock();

//                 final boolean firstRun = migrationSuccessCount == 0;

//                 MetaDataTableRow metaDataTableRow = null;

//                 SchemaVersion currentSchemaVersion = metaDataTable.getCurrentSchemaVersion();
//                 if (firstRun) {
//                     LOG.info("Current schema version: " + currentSchemaVersion);
//                 }

//                 SchemaVersion latestAvailableMigrationVersion = migrations.get(0).getVersion();
//                 boolean isFutureMigration = latestAvailableMigrationVersion.compareTo(currentSchemaVersion) < 0;
//                 if (isFutureMigration) {
//                     LOG.warn("Database version (" + currentSchemaVersion + ") is newer than the latest available migration ("
//                              + latestAvailableMigrationVersion + ") !");
//                 }

//                 MigrationState currentSchemaState = metaDataTable.getCurrentSchemaState();
//                 if (currentSchemaState == MigrationState.FAILED) {
//                     if (isFutureMigration && ignoreFailedFutureMigration) {
//                         LOG.warn("Detected failed migration to version " + currentSchemaVersion + " !");
//                     } else {
//                         throw new MigrationException(currentSchemaVersion, false);
//                     }
//                 }

//                 if (!isFutureMigration) {
//                     Migration migration = getNextMigration(migrations, currentSchemaVersion);
//                     if (migration != null) {
//                         metaDataTableRow = applyMigration(migration);
//                     }
//                 }

//                 handle.commit();

//                 if (metaDataTableRow == null) {
//                     // No further migrations available
//                     break;
//                 }

//                 if (MigrationState.FAILED == metaDataTableRow.getState()) {
//                     throw new MigrationException(metaDataTableRow.getVersion(), false);
//                 }

//                 migrationSuccessCount++;
//             }
//             catch (Exception e) {
//                 handle.rollback();
//                 throw Migratory.processException(e);
//             }
//         }

//         logSummary(migrationSuccessCount);
//         return migrationSuccessCount;
//     }

//     /**
//      * Logs the summary of this migration run.
//      */
//     private void logSummary(int migrationSuccessCount) {
//         if (migrationSuccessCount == 0) {
//             LOG.info("Schema is up to date. No migration necessary.");
//         } else if (migrationSuccessCount == 1) {
//             LOG.info("Migration completed. Successfully applied 1 migration.");
//         } else {
//             LOG.info("Migration completed. Successfully applied " + migrationSuccessCount + " migrations.");
//         }
//     }

//     /**
//      * Applies this migration to the database. The migration state and the execution time are updated accordingly.
//      */
//     public final MetaDataTableRow applyMigration(final Migration migration) throws SQLException
//     {
//         MetaDataTableRow metaDataTableRow = new MetaDataTableRow(migration);

//         LOG.info("Migrating to version " + migration.getVersion());

//         MigrationState state = null;

//         StopWatch stopWatch = new StopWatch();
//         stopWatch.start();
//         try {
//             dbi.inTransaction(new TransactionCallback<Void>() {
//                 @Override
//                 public Void inTransaction(final Handle migrationHandle, final TransactionStatus status) {
//                     migration.migrate(migrationHandle, dbSupport);
//                     return null;

//                 }
//             });
//             state = MigrationState.SUCCESS;
//         }
//         catch (Exception e) {
//             state = MigrationState.FAILED;
//             LOG.error(e.toString());
//             Throwable rootCause = ExceptionUtils.getRootCause(e);
//             LOG.error("Caused by " + rootCause.toString());
//         }

//         stopWatch.stop();
//         int executionTime = (int) stopWatch.getTime();

//         if (MigrationState.FAILED.equals(state) && dbSupport.supportsDdlTransactions()) {
//             throw new MigrationException(migration.getVersion(), true);
//         }
//         LOG.debug(String.format("Finished migrating to version %s (execution time %s)",
//                 migration.getVersion(), TimeFormat.format(executionTime)));

//         metaDataTableRow.update(executionTime, state);
//         metaDataTable.insert(metaDataTableRow);

//         return metaDataTableRow;
//     }

//     /**
//      * Returns the next migration to apply.
//      */
//     private Migration getNextMigration(List<Migration> allMigrations, SchemaVersion currentVersion) {
//         if (target.compareTo(currentVersion) < 0) {
//             LOG.warn("Database version (" + currentVersion + ") is newer than the target version ("
//                     + target + ") !");
//             return null;
//         }

//         Migration nextMigration = null;
//         for (Migration migration : allMigrations) {
//             if ((migration.getVersion().compareTo(currentVersion) > 0)) {
//                 nextMigration = migration;
//             } else {
//                 break;
//             }
//         }

//         if (nextMigration == null) {
//             return null;
//         }

//         if (target.compareTo(nextMigration.getVersion()) < 0) {
//             return null;
//         }

//         return nextMigration;
//     }

// }
