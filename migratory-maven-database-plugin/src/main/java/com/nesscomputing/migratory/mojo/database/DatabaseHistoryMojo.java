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
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
import com.nesscomputing.migratory.mojo.database.util.MojoLocator;


/**
 * Maven goal to report the migration history of the databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal history
 */
public class DatabaseHistoryMojo extends AbstractDatabaseMojo
{
    private static final Log CONSOLE = Log.forName("console");

    private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateHourMinuteSecond();

    /**
     * @parameter expression="${databases}" default-value="all"
     */
    private String databases = "all";

    @Override
    protected void doExecute() throws Exception
    {
        final List<String> databaseList = expandDatabaseList(databases);

        final boolean permission = config.getBoolean(getPropertyName("permission.history-db"), true);
        if (!permission) {
            throw new MojoExecutionException("No permission to run this task!");
        }

        CONSOLE.info(FRAME);
        CONSOLE.info(HEADER);
        CONSOLE.info(FRAME);

        for (String database : databaseList) {

            final Map<String, MigrationInformation> availableMigrations = getAvailableMigrations(database);

            final DBIConfig databaseConfig = getDBIConfigFor(database);
            final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
            final DBI dbi = getDBIFor(database);

            try {
                final Migratory migratory = new Migratory(migratoryConfig, dbi, rootDbDbi);
                migratory.addLocator(new MojoLocator(migratory, manifestUrl));
                final Map<String, List<MetadataInfo>> results = migratory.dbHistory(availableMigrations.keySet(), optionList);

                dump(database, results);
                CONSOLE.info(FRAME);
            }
            catch (MigratoryException me) {
                CONSOLE.warnDebug(me, "While getting history for %s", database);
            }
            catch (RuntimeException re) {
                CONSOLE.warnDebug(re, "While getting history for %s", database);
            }
        }
    }

    private static final String FRAME  = "+---------------------------+---------------------------+-----------+------+-------+-----+--------------------+---------------------+";
    private static final String HEADER = "|         Database          |        Personality        | Migration | Type | State | Dir |       User         | Date                |";
    private static final String BODY   = "| %-25s | %-25s | %4d-%-4d | %-4s | %-5s | %-3s | %-18s | %18s |";

    public static void dump(final String database, final Map<String, List<MetadataInfo>> results)
    {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (final Map.Entry<String, List<MetadataInfo>> result : results.entrySet()) {
            final String personalityName = result.getKey();
            for (final MetadataInfo info : result.getValue()) {

                CONSOLE.info(String.format(BODY,
                                       database,
                                       personalityName,
                                       info.getStartVersion(),
                                       info.getEndVersion(),
                                       info.getType(),
                                       info.getState(),
                                       shortDir(info.getDirection()),
                                       info.getUser(),
                                       DATE_FORMAT.print(info.getCreated())
                             ));
            }
        }
    }



    private static String shortDir(final MigrationDirection dir)
    {
        switch (dir) {
        case FORWARD:
            return "FWD";
        case BACK:
            return "BCK";
        default:
            return dir.name();
        }
    }
}
