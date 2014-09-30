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

import java.util.List;

import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes a given database. Ensures that the metadata table exists and has the latest available version.
 */
class InternalInit extends AbstractMigratorySupport
{
    private static final Logger LOG = LoggerFactory.getLogger(InternalInit.class);

    private final MigratoryContext migratoryContext;

    InternalInit(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
    }

    List<MetadataInfo> init(final MigratoryOption [] options)
    {
        LOG.debug("Running init({})", String.valueOf(options));
        if (migratoryContext.getConfig().isReadOnly()) {
            throw new MigratoryException(Reason.IS_READONLY);
        }

        final MetadataManager metadataManager = new MetadataManager(migratoryContext);
        return metadataManager.ensureMetadata(options);
    }
}
