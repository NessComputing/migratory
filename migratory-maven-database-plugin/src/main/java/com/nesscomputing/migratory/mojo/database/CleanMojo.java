package com.nesscomputing.migratory.mojo.database;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;


/**
 * Maven goal that drops all database objects.
 *
 * @aggregator true
 * @requiresProject false
 * @goal clean
 */
public class CleanMojo extends AbstractDatabaseMojo
{
    private static final Logger LOG = LoggerFactory.getLogger(CleanMojo.class);

    /**
     * @parameter expression="${databases}"
     * @required
     */
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
            LOG.info("Cleaning Database {}...", database);

            final DBIConfig databaseConfig = getDBIConfigFor(database);
            final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
            final DBI dbi = getDBIFor(database);

            try {
                final Migratory migratory = new Migratory(migratoryConfig, dbi, rootDbDbi);
                migratory.dbClean(optionList);
            }
            catch (MigratoryException me) {
                LOG.warn("While cleaning {}: {}", database, me);
            }
            catch (RuntimeException re) {
                LOG.warn("While cleaning {}: {}", database, re);
            }

            LOG.info("... done");
        }
    }
}
