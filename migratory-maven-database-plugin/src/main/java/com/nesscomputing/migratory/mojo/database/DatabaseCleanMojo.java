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
package com.nesscomputing.migratory.mojo.database;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Maven goal that drops all database objects.
 *
 * @aggregator true
 * @requiresProject false
 * @goal clean
 */
public class DatabaseCleanMojo extends AbstractDatabaseMojo
{
    private static final Log CONSOLE = Log.forName("console");

    /**
     * @parameter expression="${databases}"
     * @required
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String databases;

    @Override
    protected void doExecute() throws Exception
    {
        final List<String> databaseList = expandDatabaseList(databases);

        final boolean permission = config.getBoolean(getPropertyName("permission.clean-db"), false);
        if (!permission) {
            throw new MojoExecutionException("No permission to run this task!");
        }

        for (String database : databaseList) {
            CONSOLE.info("Cleaning Database %s...", database);

            final DBIConfig databaseConfig = getDBIConfigFor(database);
            final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
            final DBI dbi = getDBIFor(database);

            try {
                final Migratory migratory = new Migratory(migratoryConfig, dbi, rootDbDbi);
                migratory.dbClean(optionList);
            }
            catch (MigratoryException me) {
                CONSOLE.warnDebug(me, "While cleaning %s", database);
            }
            catch (RuntimeException re) {
                CONSOLE.warnDebug(re, "While cleaning %s", database);
            }

            CONSOLE.info("... done");
        }
    }
}
