package com.nesscomputing.migratory.mojo.database;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.skife.jdbi.v2.util.IntegerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
import com.nesscomputing.migratory.mojo.database.util.TemplatingStatementLocator;


/**
 * Maven goal that creates all databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal create
 */
public class CreateMojo extends AbstractDatabaseMojo
{
    private static final Logger LOG = LoggerFactory.getLogger(CreateMojo.class);

    /**
     * @parameter expression="${databases}"
     * @required
     */
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
                LOG.info("Dry run for database {} activated!", database);
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
                        LOG.trace("... User {} already exists ...", user);
                    }
                    else {
                        LOG.info("... creating User {} ...", user);

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
                        LOG.info("... Database {} already exists ...", database);
                    }
                    else {
                        LOG.info("... creating Database {}...", database);

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
                                LOG.warn("Tablespace '" + databaseConfig.getDBTablespace() + "' does not exist, falling back to default!");
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
                    LOG.warn("While creating {}: {}", database, de);
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
                        LOG.trace("Language plpgsql exists");
                    }
                    else {
                        LOG.info("... creating plpgsql language...");

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
                    LOG.warn("While creating {}: {}", database, de);
                }

                final boolean createSchema = config.getBoolean(getPropertyName("schema.create"), false);

                if (createSchema) {
                    final String schemaName = databaseConfig.getDBUser();
                    try {
                        if (detectSchema(rootDbDbi, schemaName)) {
                            LOG.trace("Schema {} exists", schemaName);
                        }
                        else {
                            LOG.info("... creating Schema {} ...", schemaName);

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
                        LOG.warn("While creating schema {}: {}", schemaName, de);
                    }

                    final boolean enforceSchema = config.getBoolean(getPropertyName("schema.enforce"), false);

                    if (enforceSchema) {
                        try {
                            if (!detectSchema(rootDbDbi, "public")) {
                                LOG.trace("public schema does not exist");
                            }
                            else {
                                LOG.info("... dropping public schema ...");

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
                            LOG.warn("While dropping public schema: {}", de);
                        }
                    }
                }
                else {
                    LOG.info("... not creating schema ...");
                }


                try {
                    LOG.info("... initializing metadata ...");

                    // Finally metadata is created as the database owner connected to the database.

                    final DBI dbi = getDBIFor(database);

                    Migratory migratory = new Migratory(migratoryConfig, dbi);
                    migratory.dbInit();
                }
                catch (DBIException de) {
                    LOG.warn("While creating {}: {}", database, de);
                }
                catch (MigratoryException me) {
                    LOG.warn("While creating {}: {}", database, me);
                }
            }
            LOG.info("... done");
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
