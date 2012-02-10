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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.DbMigrator;
import com.nesscomputing.migratory.migration.MigrationManager;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationPlanner;
import com.nesscomputing.migratory.migration.MigrationResult;
import com.nesscomputing.migratory.migration.MigrationPlan.MigrationPlanEntry;
import com.nesscomputing.migratory.validation.DbValidator;
import com.nesscomputing.migratory.validation.ValidationResult;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationStatus;

class InternalMigrator extends AbstractMigratorySupport
{
    private static final Logger LOG = LoggerFactory.getLogger(InternalMigrator.class);

    private final MigratoryContext migratoryContext;
    private final MigratoryConfig migratoryConfig;

    InternalMigrator(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
        this.migratoryConfig = migratoryContext.getConfig();
    }

    Map<String, List<MetadataInfo>> migrate(final MigrationPlan migrationPlan, final MigratoryOption [] options) throws MigratoryException
    {
        if (migratoryConfig.isReadOnly()) {
            throw new MigratoryException(Reason.IS_READONLY);
        }

        final MetadataManager metadataManager = new MetadataManager(migratoryContext);

        try {
            metadataManager.ensureMetadata(options);

            final Map<String, List<MetadataInfo>> migrationResults = Maps.newTreeMap();
            final Collection<String> existingPersonalities = metadataManager.retrieveExistingPersonalities();

            MigrationPlan planToExecute = migrationPlan;

            // If no personalities were give, migrate all existing personalities to the latest and greatest version.
            if (planToExecute == null || planToExecute.isEmpty()) {
                planToExecute = new MigrationPlan();
                for (String existingPersonality : existingPersonalities) {
                    migrationPlan.addMigration(existingPersonality);
                }
            }

            // Make sure that all requested personalities either already exist in the
            // database or that they can be created.
            for (final MigrationPlanEntry migrationPlanEntry : planToExecute.getEntries()) {
                final String personalityName = migrationPlanEntry.getPersonalityName();

                try {
                    metadataManager.lock(personalityName);
                    final List<MigrationResult> results = migratePersonality(metadataManager, personalityName, migrationPlanEntry.getTargetVersion(), options);
                    final List<MetadataInfo> personalityMigrationResult = metadataManager.commit(results);
                    if (!personalityMigrationResult.isEmpty()) {
                        migrationResults.put(personalityName, personalityMigrationResult);
                    }
                }
                catch (MigratoryException me) {
                    metadataManager.rollback();
                    throw me;
                }
                catch (RuntimeException re) {
                    metadataManager.rollback();
                    throw re;
                }
            }
            return migrationResults;
        }
        catch (Exception e) {
            throw processException(e);
        }
    }

    /**
     * Performs the migration of a personality. This must be run under the table lock so that only one thread can use it at a time.
     */
    private List<MigrationResult> migratePersonality(final MetadataManager metadataManager, final String personalityName, final Integer targetVersion, final MigratoryOption [] options)
    {
        final Integer currentVersion = metadataManager.getCurrentVersion(personalityName);

        if (currentVersion == null  && !migratoryConfig.isCreatePersonalities()) {
            throw new MigratoryException(Reason.NEW_PERSONALITIES_DENIED);
        }

        // Make sure that the current state of the personality is sane.
        final List<MetadataInfo> history = metadataManager.getPersonalityHistory(personalityName);

        final MigrationManager migrationManager = new MigrationManager(migratoryContext, personalityName);

        // if null, this is a new personality.Don't validate it.
        if (history != null && !history.isEmpty()) {
            // "No verify" option skips this step.
            if (!MigratoryOption.containsOption(MigratoryOption.NO_VERIFY, options)) {
                final DbValidator dbValidator = new DbValidator(migrationManager);
                final ValidationResult validationResult = dbValidator.validate(history);

                if (validationResult.getValidationStatus() != ValidationStatus.OK) {
                    throw new MigratoryException(Reason.VALIDATION_FAILED, "Validation for Personality '%s' failed", personalityName);
                }
            }
            else {
                LOG.info("Skipped verification.");
            }
        }

        final MigrationPlanner migrationPlanner = new MigrationPlanner(migrationManager, currentVersion, targetVersion);

        migrationPlanner.plan();

        LOG.info(migrationPlanner.toString());
        switch(migrationPlanner.getDirection()) {
        case FORWARD:
            if (!migratoryConfig.isAllowRollForward()) {
                throw new MigratoryException(Reason.ROLL_FORWARD_DENIED);
            }
            break;

        case BACK:
            if (!migratoryConfig.isAllowRollBack()) {
                throw new MigratoryException(Reason.ROLL_BACK_DENIED);
            }
            break;

        case DO_NOTHING:
            return null;

        default:
            LOG.warn("Encountered State {}. This should never happen!", migrationPlanner.getDirection());
            return null;
        }

        final DbMigrator migrator = new DbMigrator(migratoryContext, migrationPlanner);
        final List<MigrationResult> results = migrator.migrate(options);
        LOG.info("Migration successful in '{}' steps.", results.size());
        return results;
    }
}
