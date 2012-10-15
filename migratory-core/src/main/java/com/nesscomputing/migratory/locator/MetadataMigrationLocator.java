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
package com.nesscomputing.migratory.locator;


import java.net.URI;
import java.util.Map;

import com.google.common.collect.Maps;

import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.metadata.MetadataManager;

/**
 * Provides the internal migrations for the metadata table.
 */
public class MetadataMigrationLocator extends AbstractSqlResourceLocator
{
    // Keep the trailing slash!
    public static final String METADATA_LOCATION = "metadata:/migratory/metadata/";
    public static final String METADATA_PATTERN = "migratory_metadata.*";

    public MetadataMigrationLocator(final MigratoryContext migratoryContext)
    {
        super(migratoryContext);
    }

    @Override
    protected Map.Entry<URI, String> getBaseInformation(final String personalityName, final String databaseType)
    {
        // Knows only about the basic migrations.
        if (MetadataManager.METADATA_MIGRATION_NAME.equals(personalityName)) {
            return Maps.immutableEntry(URI.create(METADATA_LOCATION + databaseType), METADATA_PATTERN);
        }
        return null;
    }

    @Override
    public boolean isSystemLoader()
    {
        return true;
    }
}
