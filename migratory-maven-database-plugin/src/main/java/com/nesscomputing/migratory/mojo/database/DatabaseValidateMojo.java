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
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.mojo.database.util.DBIConfig;
import com.nesscomputing.migratory.mojo.database.util.MojoLocator;
import com.nesscomputing.migratory.validation.ValidationResult;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationResultProblem;


/**
 * Maven goal to validate databases.
 *
 * @aggregator true
 * @requiresProject false
 * @goal validate
 */
public class DatabaseValidateMojo extends AbstractDatabaseMojo
{
    private static final Logger CONSOLE = LoggerFactory.getLogger("console");

    /**
     * @parameter expression="${databases}" default-value="all"
     */
    private String databases = "all";

    @Override
    protected void doExecute() throws Exception
    {
        final List<String> databaseList = expandDatabaseList(databases);

        final boolean permission = config.getBoolean(getPropertyName("permission.validate-db"), true);
        if (!permission) {
            throw new MojoExecutionException("No permission to run this task!");
        }

        CONSOLE.info("{}", HEAD_FRAME);
        CONSOLE.info("{}", HEADER);
        CONSOLE.info("{}", HEAD_FRAME);

        for (String database : databaseList) {

            final Map<String, MigrationInformation> availableMigrations = getAvailableMigrations(database);

            final DBIConfig databaseConfig = getDBIConfigFor(database);
            final DBI rootDbDbi = new DBI(databaseConfig.getDBUrl(), rootDBIConfig.getDBUser(), rootDBIConfig.getDBPassword());
            final DBI dbi = getDBIFor(database);

            try {
                final Migratory migratory = new Migratory(migratoryConfig, dbi, rootDbDbi);
                migratory.addLocator(new MojoLocator(migratory, manifestUrl));
                final Map<String, ValidationResult> results = migratory.dbValidate(availableMigrations.keySet(), optionList);

                dump(database, results);
                CONSOLE.info("{}", HEAD_FRAME);
            }
            catch (MigratoryException me) {
                CONSOLE.warn("While validating for {}", database, me);
            }
            catch (RuntimeException re) {
                CONSOLE.warn("While validating for {}", database, re);
            }
        }
    }

    private static final String FRAME      = "+----------------------+----+---------------------------+---------+--+---------------------------------------+";
    private static final String HEAD_FRAME = "+---------------------------+---------------------------+------------+---------------------------------------+";
    private static final String HEADER     = "|         Database          |        Personality        |    State   |                                       |";

    private static final String PROB_FRAME = "+----------------------+------------------------------------------+------------------------------------------+";
    private static final String PROBLEM    = "|        Problem       | Script                                   |                  Reason                  |";
    private static final String BODY       = "| %-25s | %-25s | %-10s |                                       |";
    private static final String PROBLEM_BODY   = "| %-20s | %-40s | %-40s |";

    public static void dump(final String database, final Map<String, ValidationResult> results)
    {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (final Map.Entry<String, ValidationResult> result : results.entrySet()) {
            final String personalityName = result.getKey();
            final ValidationResult validationResult = result.getValue();

            final List<ValidationResultProblem> problems = validationResult.getProblems();
            if (!problems.isEmpty()) {
                CONSOLE.info("{}", HEAD_FRAME);
            }

            CONSOLE.info("{}", String.format(BODY,
                         database,
                         personalityName,
                         validationResult.getValidationStatus()));

            if (!problems.isEmpty()) {
                CONSOLE.info("{}", FRAME);
                CONSOLE.info("{}", PROBLEM);
                CONSOLE.info("{}", PROB_FRAME);
                for (ValidationResultProblem problem: problems) {
                    CONSOLE.info("{}", String.format(PROBLEM_BODY,
                                 problem.getValidationStatus(),
                                 problem.getMetadataInfo().getScriptName(),
                                 problem.getReason()));
                }
                CONSOLE.info("{}", FRAME);
            }
        }
    }
}
