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


import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.mojo.database.util.TemplatingStatementLocator;


/**
 * Maven goal that drops all databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal drop
 */
public class DropMojo extends AbstractDatabaseMojo
{
    private static final Logger LOG = LoggerFactory.getLogger(DropMojo.class);

    /**
     * @parameter expression="${databases}"
     * @required
     */
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
                    LOG.info("Dropping Database {}...", database);

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

                    LOG.info("... done");
                }
                else {
                    LOG.info("... Database {} does not exist ...", database);
                }
            }
            catch (DBIException de) {
                LOG.warn("While dropping {}: {}", database, de);
            }
        }
    }
}
