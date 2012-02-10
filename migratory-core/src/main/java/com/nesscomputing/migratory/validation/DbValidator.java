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
package com.nesscomputing.migratory.validation;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.Migration;
import com.nesscomputing.migratory.migration.MigrationManager;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationStatus;


public class DbValidator
{
    private final MigrationManager migrationManager;

    public DbValidator(final MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }

    public ValidationResult validate(final List<MetadataInfo> metadataInfoList)
    {
        final ValidationResult.Builder builder = new ValidationResult.Builder();
        final Map<String, Migration> migrations = migrationManager.getMigrations();

        for (MetadataInfo metadataInfo : metadataInfoList) {
            final Migration matchingMigration = migrations.get(metadataInfo.getScriptName());
            if (matchingMigration == null) {
                builder.add(ValidationStatus.NOT_FOUND, metadataInfo, "");
            }
            else {
                if (!StringUtils.equals(matchingMigration.getChecksum(), metadataInfo.getChecksum())) {
                    builder.add(ValidationStatus.BAD_CHECKSUM, metadataInfo, matchingMigration.getChecksum());
                }
                if (matchingMigration.getType() != metadataInfo.getType()) {
                    builder.add(ValidationStatus.TYPE_MISMATCH, metadataInfo, matchingMigration.getType().name());
                }
            }
        }

        // Get the last element in the list.
        final MetadataInfo lastInfo = metadataInfoList.get(metadataInfoList.size()-1);
        if (lastInfo.getState() == MigrationState.FAIL) {
            builder.add(ValidationStatus.FAILED_STATE, lastInfo, "");
        }

        return builder.build();
    }
}
