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


import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.maven.util.FormatInfo;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Maven goal that shows the history (all applied migrations) of the database.
 *
 * @aggregator true
 * @requiresProject false
 * @goal history
 */
public class HistoryMojo extends AbstractMigratoryMojo
{
    private static final Logger LOG = LoggerFactory.getLogger(HistoryMojo.class);

    private static final FormatInfo SHORT = new FormatInfo(
       "+-----------+------+-------+-----+--------------------+---------------------+",
       "| %-73s |\n",
       "| Migration | Type | State | Dir |       User         | Date                |",
       "| %4d-%-4d | %-4s | %-5s | %-3s | %-18s | %18s |\n"
    );


    private static final FormatInfo VERBOSE = new FormatInfo(
       "+-----------+------+-------+-----+-----------+--------------------------------+-------+------------------------------------------+------------------------------------------+",
       "| %-169s |\n",
       "| Migration | Type | State | Dir |        User          | Date                | Time  | Description                              | Script                                   |",
       "| %4d-%-4d | %-4s | %-5s | %-3s | %-20s | %18s | %5d | %-40s | %-40s |\n"
    );

    private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateHourMinuteSecond();

    /**
     * @parameter expression="${verbose}"
     */
    private boolean verbose = false;

    /**
     * @parameter expression="${personalities}"
     */
    @SuppressFBWarnings("UWF_NULL_FIELD")
    private String personalities = null;

    @Override
    protected void doExecute(Migratory migratory) throws Exception
    {
        final List<String> personalityList = parsePersonalities(personalities);

        final Map<String, List<MetadataInfo>> results = migratory.dbHistory(personalityList, optionList);
        for (Map.Entry<String, List<MetadataInfo>> personality : results.entrySet()) {
            dump(verbose, personality.getKey(), personality.getValue());
        }
    }

    public static void dump(final boolean verbose, final String personality, final List<MetadataInfo> info)
    {
        if (info == null || info.isEmpty()) {
            return;
        }

        final FormatInfo formatInfo = verbose ? VERBOSE : SHORT;

        LOG.info(formatInfo.getFrame());
        LOG.info(String.format(formatInfo.getName(), personality));
        LOG.info(formatInfo.getFrame());
        LOG.info(formatInfo.getHeader());
        LOG.info(formatInfo.getFrame());
        for (MetadataInfo metadataInfo : info) {
            LOG.info(String.format(formatInfo.getFormat(),
                                   metadataInfo.getStartVersion(),
                                   metadataInfo.getEndVersion(),
                                   metadataInfo.getType(),
                                   metadataInfo.getState(),
                                   shortDir(metadataInfo.getDirection()),
                                   metadataInfo.getUser(),
                                   DATE_FORMAT.print(metadataInfo.getCreated()),
                                   metadataInfo.getExecutionTime(),
                                   metadataInfo.getDescription(),
                                   metadataInfo.getScriptName()
                ));
        }
        LOG.info(formatInfo.getFrame());
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
