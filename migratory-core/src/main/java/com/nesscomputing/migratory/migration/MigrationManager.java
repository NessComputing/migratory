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


import java.util.Collection;
import java.util.Map;


import com.google.common.collect.Maps;
import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.locator.MigrationLocator;
import com.nesscomputing.migratory.metadata.MetadataManager;

public class MigrationManager
{
    private final Map<String, Migration> availableMigrations = Maps.newHashMap();
    private final String personalityName;

    public MigrationManager(final MigratoryContext migratoryContext, final String personalityName)
    {
        this.personalityName = personalityName;

        final String databaseType = migratoryContext.getDbSupport().getDatabaseType();
        final Collection<MigrationLocator> migrationLocators = migratoryContext.getLocators();

        // Find all available migrations for this personality with this database type.
        for (MigrationLocator loader : migrationLocators) {
            // Load the internal migrations only from a loader that knows how to find them.
                if (MetadataManager.METADATA_MIGRATION_NAME.equals(personalityName)) {
                    if (loader.isSystemLoader()) {
                        loader.contributeMigrations(availableMigrations, personalityName, databaseType);
                    }
                }
                else {
                    if (!loader.isSystemLoader()) {
                        loader.contributeMigrations(availableMigrations, personalityName, databaseType);
                    }
                }
        }
    }

    public Map<String, Migration> getMigrations()
    {
        return availableMigrations;
    }

    public String getPersonalityName()
    {
        return personalityName;
    }
}

