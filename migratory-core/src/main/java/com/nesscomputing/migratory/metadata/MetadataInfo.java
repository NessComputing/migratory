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
package com.nesscomputing.migratory.metadata;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.nesscomputing.migratory.jdbi.JdbiArguments;
import com.nesscomputing.migratory.jdbi.JdbiMappers;
import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;
import com.nesscomputing.migratory.migration.MigrationResult;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.migration.MigrationType;

public class MetadataInfo
{
    public static final Mapper MAPPER = new Mapper();

    /** Starting point of this migration. */
    private final int startVersion;

    /** Ending point of this migration. */
    private final int endVersion;

    /** Personality that was migrated. */
    private final String personalityName;

    /** Description of this migration. */
    private final String description;

    /** Migration Type (SQL for SQL scripts, JAVA for java classes). */
    private final MigrationType migrationType;

    /** Migration State (OK or FAIL). */
    private final MigrationState state;

    /** Migration direction. */
    private final MigrationDirection direction;

    /** Execution time in milliseconds. */
    private final long executionTime;

    /** Script location (URI for SQL, class name for Java */
    private final String location;

    /** Script name */
    private final String scriptName;

    /** Checksum (SHA1 for SQL scripts, Java classes can provide their own information. */
    private final String checksum;

    /** User that ran the migration (information from the database. */
    private final String user;

    /** Time of the Migration execution. */
    private final DateTime created;

    /** Primary key. */
    private final long metadataInfoId;

    MetadataInfo(final int startVersion,
                        final int endVersion,
                        final String personalityName,
                        final String description,
                        final MigrationType migrationType,
                        final MigrationState state,
                        final MigrationDirection direction,
                        final long executionTime,
                        final String location,
                        final String scriptName,
                        final String checksum,
                        final String user,
                        final DateTime created,
                        final long metadataInfoId)
    {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
        this.personalityName = personalityName;
        this.description = description;
        this.migrationType = migrationType;
        this.state = state;
        this.direction = direction;
        this.executionTime = executionTime;
        this.location = location;
        this.scriptName = scriptName;
        this.checksum = checksum;
        this.user = user;
        this.created = created;
        this.metadataInfoId = metadataInfoId;
    }

    public MetadataInfo(final MigrationResult migrationResult)
    {
        this(migrationResult.getMigration().getStartVersion(),
             migrationResult.getMigration().getEndVersion(),
             migrationResult.getMigration().getPersonalityName(),
             migrationResult.getMigration().getDescription(),
             migrationResult.getMigration().getType(),
             migrationResult.getState(),
             migrationResult.getDirection(),
             migrationResult.getExecutionTime(),
             migrationResult.getMigration().getLocation(),
             migrationResult.getMigration().getScriptName(),
             migrationResult.getMigration().getChecksum(),
             null,
             null,
             -1);
    }

    public int getStartVersion()
    {
        return startVersion;
    }

    public int getEndVersion()
    {
        return endVersion;
    }

    public String getPersonalityName()
    {
        return personalityName;
    }

    public String getDescription()
    {
        return description;
    }

    public MigrationType getType()
    {
        return migrationType;
    }

    public MigrationState getState()
    {
        return state;
    }

    public MigrationDirection getDirection()
    {
        return direction;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public String getLocation()
    {
        return location;
    }

    public String getScriptName()
    {
        return scriptName;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public String getUser()
    {
        return user;
    }

    public DateTime getCreated()
    {
        return created;
    }

    public long getMetadataInfoId()
    {
        return metadataInfoId;
    }

    public Update bindToHandle(final Update update)
    {
        update.bind("start_version", getStartVersion());
        update.bind("end_version",getEndVersion());
        update.bind("personality_name",getPersonalityName());
        update.bind("description",getDescription());
        update.bind("type",JdbiArguments.forEnum(getType()));
        update.bind("state",JdbiArguments.forEnum(getState()));
        update.bind("direction",JdbiArguments.forEnum(getDirection()));
        update.bind("execution_time",getExecutionTime());
        update.bind("location",getLocation());
        update.bind("script_name",getScriptName());
        update.bind("checksum",getChecksum());

        return update;
    }

    static class Mapper implements ResultSetMapper<MetadataInfo>
    {
        @Override
        public MetadataInfo map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException
        {
            return new MetadataInfo(
                r.getInt("start_version"),
                r.getInt("end_version"),
                r.getString("personality_name"),
                r.getString("description"),
                JdbiMappers.getEnum(r, MigrationType.class, "type"),
                JdbiMappers.getEnum(r, MigrationState.class, "state"),
                JdbiMappers.getEnum(r, MigrationDirection.class, "direction"),
                r.getLong("execution_time"),
                r.getString("location"),
                r.getString("script_name"),
                r.getString("checksum"),
                r.getString("metadata_user"),
                JdbiMappers.getDateTime(r, "metadata_created"),
                r.getLong("metadata_id")
            );
        }
    }

    private transient String toString;

    @Override
    public String toString()
    {
        if (toString == null) {
            toString =
                new ToStringBuilder(this).append("startVersion", startVersion)
                    .append("endVersion", endVersion)
                    .append("personalityName", personalityName)
                    .append("description", description)
                    .append("migrationType", migrationType)
                    .append("state", state)
                    .append("direction", direction)
                    .append("executionTime", executionTime)
                    .append("location", location)
                    .append("script_name", scriptName)
                    .append("checksum", checksum)
                    .append("user", user)
                    .append("created", created)
                    .append("metadataInfoId", metadataInfoId)
                    .toString();
        }
        return toString;
    }

    public static final MigrationState determineMigrationState(final List<MetadataInfo> migrationResults)
    {
        if (migrationResults == null || migrationResults.size() == 0) {
            return MigrationState.UNKNOWN;
        }
        return migrationResults.get(migrationResults.size() - 1).getState();
    }

}
