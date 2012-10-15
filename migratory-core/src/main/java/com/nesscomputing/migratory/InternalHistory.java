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

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;

class InternalHistory extends AbstractMigratorySupport
{
    private static final Log LOG = Log.findLog();

    private final MigratoryContext migratoryContext;

    InternalHistory(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
    }

    Map<String, List<MetadataInfo>> history(final Collection<String> personalities, final MigratoryOption [] options)
    {
        LOG.debug("Running history(%s)", personalities);

        final MetadataManager manager = new MetadataManager(migratoryContext);
        try {
            manager.ensureMetadata(options);

            return manager.getHistory(personalities, options);
        }
        catch (Exception e) {
            throw processException(e);
        }
    }
}
