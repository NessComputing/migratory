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



import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.StatusResult;
import com.nesscomputing.migratory.maven.util.FormatInfo;
import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;

/**
 * Maven goal that shows the status (current version) of the database.
 *
 * @aggregator true
 * @requiresProject false
 * @goal status
 */
public class StatusMojo extends AbstractMigratoryMojo
{
    private static final FormatInfo SHORT = new FormatInfo(
       "+--------------------------------+-------+------+-------+------+---------+-----+",
       null,
       "|          Personality           | State | Curr | First | Last | Migrate | Dir |",
       "| %-30s | %-5s | %4d |  %4s | %4s |    %1s    | %-3s |\n"
    );

    /**
     * @parameter expression="${personalities}"
     */
    protected String personalities;

    @Override
    protected void doExecute(final Migratory migratory) throws Exception
    {
        final List<String> personalityList = parsePersonalities(personalities);

        final Map<String, StatusResult> results = migratory.dbStatus(personalityList, optionList);
        dump(results.values());
    }

    public static void dump(final Collection<StatusResult> results)
    {
        if (results == null || results.isEmpty()) {
            return;
        }

        final FormatInfo formatInfo = SHORT;

        System.out.println(formatInfo.getFrame());
        System.out.println(formatInfo.getHeader());
        System.out.println(formatInfo.getFrame());

        for (StatusResult result : results) {
            System.out.printf(formatInfo.getFormat(),
                              result.getPersonalityName(),
                              result.getLastState(),
                              result.getCurrentVersion(),
                              // If the status code has no access to the available migrations (because there is
                              // no locator or loader, those will be MAX_VALUE for first and MIN_VALUE for last.
                              // In that case, ignore the output.
                              result.getFirstVersion() != Integer.MAX_VALUE ? Integer.toString(result.getFirstVersion()) : "",
                              result.getLastVersion() != Integer.MIN_VALUE ? Integer.toString(result.getLastVersion()) : "",
                              result.isMigrationPossible() ? "Y" : "N",
                              shortDir(result.getDirection())
                );
        }

        System.out.println(formatInfo.getFrame());
        System.out.println();
    }

    private static String shortDir(final MigrationDirection dir)
    {
        switch (dir) {
        case FORWARD:
            return "FWD";
        case BACK:
            return "BCK";

        default:
            return "";
        }
    }
}
