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
package com.nesscomputing.migratory.maven;


import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.locator.AbstractSqlResourceLocator;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlan;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @aggregator true
 * @requiresProject false
 * @goal migrate
 */
public class MigrateMojo extends AbstractMigratoryMojo
{
    private static final String CONTEXT_PROPERTIES= "context";

    /**
     * @parameter expression="${migrations}"
     * @required
     */
    @SuppressFBWarnings("UWF_NULL_FIELD")
    protected String migrations = null;

    /**
     * @parameter expression="${location.url}"
     * @required
     */
    @SuppressFBWarnings({"UWF_NULL_FIELD", "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    protected URL locationUrl = null;

    /**
     * @parameter expression="${verbose}"
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    protected boolean verbose = false;


    @Override
    protected void doExecute(Migratory migratory) throws Exception
    {
        Configuration c = new SystemConfiguration().subset(CONTEXT_PROPERTIES);

        for (Iterator<?> it = c.getKeys(); it.hasNext(); ) {
            final String key = (String) it.next();
            migratory.addDefine(key, c.getString(key));
        }

        final MigrationPlan plan = createMigrationPlan(migrations);
        migratory.addLocator(new MojoLocator(migratory, locationUrl.toURI()));

        migratory.dbMigrate(plan, optionList);

        final Map<String, List<MetadataInfo>> results = migratory.dbHistory(null, optionList);
        for (Map.Entry<String, List<MetadataInfo>> personality : results.entrySet()) {
            HistoryMojo.dump(verbose, personality.getKey(), personality.getValue());
        }
    }

    protected MigrationPlan createMigrationPlan(final String migrations) throws MojoExecutionException
    {
        final MigrationPlan migrationPlan = new MigrationPlan();

        if (StringUtils.isBlank(migrations)) {
            return migrationPlan;
        }

        final String [] migrationNames = StringUtils.stripAll(StringUtils.split(migrations, ","));

        for (String migrationName : migrationNames) {
            final String [] migrationFields = StringUtils.stripAll(StringUtils.split(migrationName, "@"));

            if (migrationFields == null || migrationFields.length < 1 || migrationFields.length > 2) {
                throw new MojoExecutionException("Migration " + migrationName + " is invalid.");
            }

            int targetVersion = migrationFields.length == 2 ? Integer.parseInt(migrationFields[1], 10) : Integer.MAX_VALUE;

            final String [] priorityFields = StringUtils.stripAll(StringUtils.split(migrationFields[0], ":"));

            if (priorityFields == null || priorityFields.length < 1 || priorityFields.length > 2) {
                throw new MojoExecutionException("Migration " + migrationName + " is invalid.");
            }

            int priority = priorityFields.length == 2 ? Integer.parseInt(priorityFields[1], 10) : 0;


            migrationPlan.addMigration(priorityFields[0], targetVersion, priority);
        }

        return migrationPlan;
    }

    public static class MojoLocator extends AbstractSqlResourceLocator
    {
        private final URI location;

        MojoLocator(MigratoryContext migratoryContext, final URI location)
        {
            super(migratoryContext);
            this.location = location;
        }

        @Override
        protected Entry<URI, String> getBaseInformation(String personalityName, String databaseType)
        {
            return Maps.immutableEntry(location, personalityName + ".*.sql");
        }
    }
}
