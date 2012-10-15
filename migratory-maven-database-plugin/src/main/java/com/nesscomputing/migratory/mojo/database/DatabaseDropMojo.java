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
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.skife.jdbi.v2.util.IntegerMapper;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.mojo.database.util.TemplatingStatementLocator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Maven goal that drops all databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal drop
 */
public class DatabaseDropMojo extends AbstractDatabaseMojo
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

        final boolean permission = config.getBoolean(getPropertyName("permission.drop-db"), false);
        if (!permission) {
            throw new MojoExecutionException("No permission to run this task!");
        }

        final DBI rootDbi = getDBIFor(rootDBIConfig);

        final StatementLocator statementLocator = new TemplatingStatementLocator("/sql/", loaderManager);
        rootDbi.setStatementLocator(statementLocator);

        for (final String database : databaseList) {
            try {
                boolean databaseExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                    @Override
                    public Boolean withHandle(final Handle handle) {
                        return handle.createQuery("#mojo:detect_database")
                        .bind("database", database)
                        .map(IntegerMapper.FIRST)
                        .first() != 0;
                    }
                });

                if (databaseExists) {
                    CONSOLE.info("Dropping Database %s...", database);

                    if (!MigratoryOption.containsOption(MigratoryOption.DRY_RUN, optionList)) {
                        rootDbi.withHandle(new HandleCallback<Void>() {
                            @Override
                            public Void withHandle(final Handle handle) {
                                handle.createStatement("#mojo:drop_database")
                                .define("database", database)
                                .execute();
                                return null;
                            }

                        });
                    }

                    CONSOLE.info("... done");
                }
                else {
                    CONSOLE.info("... Database %s does not exist ...", database);
                }
            }
            catch (DBIException de) {
                CONSOLE.warnDebug(de, "While dropping %s", database);
            }
        }
    }
}
