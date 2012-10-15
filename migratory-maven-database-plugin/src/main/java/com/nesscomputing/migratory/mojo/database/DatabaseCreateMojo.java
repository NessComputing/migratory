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
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
import com.nesscomputing.migratory.mojo.database.util.TemplatingStatementLocator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Maven goal that creates all databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal create
 */
public class DatabaseCreateMojo extends AbstractDatabaseMojo
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

        final boolean permission = config.getBoolean(getPropertyName("permission.create-db"), false);
        if (!permission) {
            throw new MojoExecutionException("No permission to run this task!");
        }

        final DBI rootDbi = getDBIFor(rootDBIConfig);

        final StatementLocator statementLocator = new TemplatingStatementLocator("/sql/", loaderManager);
        rootDbi.setStatementLocator(statementLocator);

        for (final String database : databaseList) {
            final DBIConfig databaseConfig = getDBIConfigFor(database);
            final String user = databaseConfig.getDBUser();

            // Language and schema creation runs as root user, but connected to the actual database.
            final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
            rootDbDbi.setStatementLocator(statementLocator);

            if (MigratoryOption.containsOption(MigratoryOption.DRY_RUN, optionList)) {
                CONSOLE.info("Dry run for database %s activated!", database);
            }
            else {
                // User and Database creation runs as root user connected to the root db
                try {
                    boolean userExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                        @Override
                        public Boolean withHandle(final Handle handle) {
                            return handle.createQuery("#mojo:detect_user")
                            .bind("user", user)
                            .map(IntegerMapper.FIRST)
                            .first() != 0;
                        }
                    });

                    if (userExists) {
                        CONSOLE.info("... User %s already exists ...", user);
                    }
                    else {
                        CONSOLE.info("... creating User %s ...", user);

                        rootDbi.withHandle(new HandleCallback<Void>() {
                            @Override
                            public Void withHandle(final Handle handle) {
                                handle.createStatement("#mojo:create_user")
                                .define("user", user)
                                .define("password", databaseConfig.getDBPassword())
                                .execute();
                                return null;
                            }
                        });
                    }

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
                        CONSOLE.info("... Database %s already exists ...", database);
                    }
                    else {
                        CONSOLE.info("... creating Database %s...", database);

                        final String tablespace;

                        if (databaseConfig.getDBTablespace() != null) {
                            boolean tablespaceExists = rootDbi.withHandle(new HandleCallback<Boolean>() {
                                @Override
                                public Boolean withHandle(final Handle handle) {
                                    return handle.createQuery("#mojo:detect_tablespace")
                                    .bind("table_space", databaseConfig.getDBTablespace())
                                    .map(IntegerMapper.FIRST)
                                    .first() != 0;
                                }
                            });

                            if (tablespaceExists) {
                                tablespace = databaseConfig.getDBTablespace();
                            }
                            else {
                                CONSOLE.warn("Tablespace '%s' does not exist, falling back to default!", databaseConfig.getDBTablespace());
                                tablespace = null;
                            }
                        }
                        else {
                            tablespace = null;
                        }

                        rootDbi.withHandle(new HandleCallback<Void>() {
                            @Override
                            public Void withHandle(final Handle handle) {
                                handle.createStatement("#mojo:create_database")
                                .define("database", database)
                                .define("owner", databaseConfig.getDBUser())
                                .define("tablespace", tablespace)
                                .execute();
                                return null;
                            }
                        });
                    }

                }
                catch (DBIException de) {
                    CONSOLE.warnDebug(de, "While creating %s", database);
                }

                try {
                    boolean languageExists = rootDbDbi.withHandle(new HandleCallback<Boolean>() {
                        @Override
                        public Boolean withHandle(final Handle handle) {
                            return handle.createQuery("#mojo:detect_language")
                            .map(IntegerMapper.FIRST)
                            .first() != 0;
                        }
                    });


                    if (languageExists) {
                        CONSOLE.trace("Language plpgsql exists");
                    }
                    else {
                        CONSOLE.info("... creating plpgsql language...");

                        rootDbDbi.withHandle(new HandleCallback<Void>() {
                            @Override
                            public Void withHandle(final Handle handle) {
                                handle.createStatement("#mojo:create_language")
                                .execute();
                                return null;
                            }
                        });
                    }
                }
                catch (DBIException de) {
                    CONSOLE.warnDebug(de, "While creating %s", database);
                }

                final boolean createSchema = config.getBoolean(getPropertyName("schema.create"), false);

                if (createSchema) {
                    final String schemaName = databaseConfig.getDBUser();
                    try {
                        if (detectSchema(rootDbDbi, schemaName)) {
                            CONSOLE.trace("Schema %s exists", schemaName);
                        }
                        else {
                            CONSOLE.info("... creating Schema %s ...", schemaName);

                            rootDbDbi.withHandle(new HandleCallback<Void>() {
                                @Override
                                public Void withHandle(final Handle handle) {
                                    handle.createStatement("#mojo:create_schema")
                                    .define("schema_name", schemaName)
                                    .execute();
                                    return null;
                                }
                            });
                        }
                    }
                    catch (DBIException de) {
                        CONSOLE.warnDebug(de, "While creating schema %s", schemaName);
                    }

                    final boolean enforceSchema = config.getBoolean(getPropertyName("schema.enforce"), false);

                    if (enforceSchema) {
                        try {
                            if (!detectSchema(rootDbDbi, "public")) {
                                CONSOLE.trace("public schema does not exist");
                            }
                            else {
                                CONSOLE.info("... dropping public schema ...");

                                rootDbDbi.withHandle(new HandleCallback<Void>() {
                                    @Override
                                    public Void withHandle(final Handle handle) {
                                        handle.createStatement("#mojo:drop_schema")
                                        .define("schema_name", "public")
                                        .execute();
                                        return null;
                                    }
                                });
                            }
                        }
                        catch (DBIException de) {
                            CONSOLE.warnDebug(de, "While dropping public schema: %s", schemaName);
                        }
                    }
                }
                else {
                    CONSOLE.info("... not creating schema ...");
                }


                try {
                    CONSOLE.info("... initializing metadata ...");

                    // Finally metadata is created as the database owner connected to the database.

                    final DBI dbi = getDBIFor(database);

                    Migratory migratory = new Migratory(migratoryConfig, dbi);
                    migratory.dbInit();
                }
                catch (DBIException de) {
                    CONSOLE.warnDebug(de, "While creating %s", database);
                }
                catch (MigratoryException me) {
                    CONSOLE.warnDebug(me, "While creating %s", database);
                }
            }
            CONSOLE.info("... done");
        }
    }

    private boolean detectSchema(final DBI dbi, final String schemaName)
    {
        return dbi.withHandle(new HandleCallback<Boolean>() {
            @Override
            public Boolean withHandle(final Handle handle) {
                return handle.createQuery("#mojo:detect_schema")
                .bind("schema_name", schemaName)
                .map(IntegerMapper.FIRST)
                .first() != 0;
            }
        });

    }
}
