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


import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.MigrationManager;
import com.nesscomputing.migratory.migration.MigrationPlanner;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;

class InternalStatus extends AbstractMigratorySupport
{
    private static final Log LOG = Log.findLog();

    private final MigratoryContext migratoryContext;

    InternalStatus(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
    }

    Map<String, StatusResult> status(final Collection<String> personalities, final MigratoryOption ... options)
    {
        LOG.debug("Running status(%s, %s)", personalities, options);

            Map<String, StatusResult> results = Maps.newTreeMap();

        final MetadataManager manager = new MetadataManager(migratoryContext);
        try {
            manager.ensureMetadata(options);

            final Map<String, MetadataInfo> metadataInfos = manager.getStatus(personalities, options);

            // return a superset of the personalities that are requested (and that should be in the
            // reply and the available ones.
            final Set<String> displayPersonalities = Sets.newHashSet();
            if (personalities != null) {
                displayPersonalities.addAll(personalities);
            }
            displayPersonalities.addAll(metadataInfos.keySet());


            for (final String personalityName : displayPersonalities) {
                final MetadataInfo metadataInfo = metadataInfos.get(personalityName);

                final int currentVersion;
                final MigrationState state;

                if (metadataInfo == null) {
                    currentVersion = 0;
                    state = MigrationState.OK;
                }
                else {
                    currentVersion = metadataInfo.getEndVersion();
                    state = metadataInfo.getState();
                }

                final MigrationManager migrationManager = new MigrationManager(migratoryContext, personalityName);
                // Build a migration path from the current version all the way to the end.
                final MigrationPlanner migrationPlanner = new MigrationPlanner(migrationManager, currentVersion, Integer.MAX_VALUE);

                boolean migrationPossible = true;
                try {
                    migrationPlanner.plan();
                }
                catch (MigratoryException me) {
                    if (me.getReason() != Reason.VALIDATION_FAILED) {
                        throw me;
                    }
                    else {
                        migrationPossible = false;
                    }
                }
                final StatusResult result = new StatusResult(personalityName,
                                                             migrationPossible,
                                                             state,
                                                             migrationPlanner.getDirection(),
                                                             currentVersion,
                                                             migrationPlanner.getFirstVersion(),
                                                             migrationPlanner.getLastVersion());

                results.put(personalityName, result);
            }
            return results;
        }
        catch (Exception e) {
            throw processException(e);
        }
    }
}
