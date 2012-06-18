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
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.loader.LoaderManager;
import com.nesscomputing.migratory.migration.Migration;
import com.nesscomputing.migratory.migration.sql.SqlMigration;

public abstract class AbstractSqlResourceLocator implements MigrationLocator
{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlResourceLocator.class);

    protected final MigratoryContext migratoryContext;
    protected final LoaderManager loaderManager;

    protected AbstractSqlResourceLocator(final MigratoryContext migratoryContext)
    {
        this.migratoryContext = migratoryContext;
        this.loaderManager = migratoryContext.getLoaderManager();
    }

    @Override
    public void contributeMigrations(final Map<String, Migration> migrations, final String personalityName, final String databaseType)
    {
        final Map.Entry<URI, String> baseInformation = getBaseInformation(personalityName, databaseType);

        if (baseInformation != null) {
            final URI baseUri = baseInformation.getKey();
            LOG.debug("Loading migrations from {}.", baseUri);

            final Collection<URI> uris = loaderManager.loadFolder(baseUri, baseInformation.getValue());

            if (uris == null) {
                throw new MigratoryException(Reason.INTERNAL, "Could not load the metadata migrations from " + baseUri + "!");
            }

            for (final URI uri : uris) {
                final String rawSql = loaderManager.loadFile(uri);

                String path = uri.getPath();
                String fileName = "unknown filename";

                if (path == null) {
                    // jar:file:/....
                    path = URI.create(uri.getSchemeSpecificPart()).getPath();
                }

                if (path != null) {
                    final int slashIndex = path.lastIndexOf('/');
                    fileName = path.substring(slashIndex == -1 ? 0 : slashIndex + 1);
                }

                final SqlMigration migration = new SqlMigration(migratoryContext, personalityName, uri, fileName, rawSql);
                migrations.put(fileName, migration);
            }
        }
    }

    protected abstract Map.Entry<URI, String> getBaseInformation(final String personalityName, final String databaseType);

    @Override
    public boolean isSystemLoader()
    {
        return false;
    }
}
