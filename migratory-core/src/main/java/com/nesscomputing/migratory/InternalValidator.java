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

import com.google.common.collect.Maps;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.MigrationManager;
import com.nesscomputing.migratory.validation.DbValidator;
import com.nesscomputing.migratory.validation.ValidationResult;

class InternalValidator extends AbstractMigratorySupport
{
    private final MigratoryContext migratoryContext;

    InternalValidator(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
    }

    Map<String, ValidationResult> validate(final Collection<String> personalities, final MigratoryOption ... options) throws MigratoryException
    {
        final MetadataManager metadataManager = new MetadataManager(migratoryContext);

        metadataManager.ensureMetadata(options);

        final Map<String, List<MetadataInfo>> recordedMigrations = metadataManager.getHistory(personalities, options);

        final Map<String, ValidationResult> results = Maps.newTreeMap();

        for (Map.Entry<String, List<MetadataInfo>> personality : recordedMigrations.entrySet()) {
            final DbValidator dbValidator = new DbValidator(new MigrationManager(migratoryContext, personality.getKey()));
            final ValidationResult result = dbValidator.validate(personality.getValue());
            results.put(personality.getKey(), result);
        }

        return results;
    }
}
