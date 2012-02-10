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
package com.nesscomputing.migratory;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;

public class StatusResult
{
    private final String personalityName;
    private final boolean migrationPossible;
    private final MigrationState lastState;
    private final MigrationDirection direction;
    private final int currentVersion;
    private final int firstVersion;
    private final int lastVersion;

    public StatusResult(final String personalityName,
                        final boolean migrationPossible,
                        final MigrationState lastState,
                        final MigrationDirection direction,
                        final int currentVersion,
                        final int firstVersion,
                        final int lastVersion)
    {
        this.personalityName = personalityName;
        this.lastState = lastState;
        this.migrationPossible = migrationPossible;
        this.direction = direction;
        this.currentVersion = currentVersion;
        this.firstVersion = firstVersion;
        this.lastVersion = lastVersion;
    }

    public String getPersonalityName()
    {
        return personalityName;
    }

    public boolean isMigrationPossible()
    {
        return migrationPossible;
    }

    public MigrationState getLastState()
    {
        return lastState;
    }

    public MigrationDirection getDirection()
    {
        return direction;
    }

    public int getCurrentVersion()
    {
        return currentVersion;
    }

    public int getFirstVersion()
    {
        return firstVersion;
    }

    public int getLastVersion()
    {
        return lastVersion;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof StatusResult))
            return false;
        StatusResult castOther = (StatusResult) other;
        return new EqualsBuilder().append(personalityName, castOther.personalityName)
            .append(migrationPossible, castOther.migrationPossible)
            .append(lastState, castOther.lastState)
            .append(direction, castOther.direction)
            .append(currentVersion, castOther.currentVersion)
            .append(firstVersion, castOther.firstVersion)
            .append(lastVersion, castOther.lastVersion)
            .isEquals();
    }

    private transient int hashCode;

    @Override
    public int hashCode()
    {
        if (hashCode == 0) {
            hashCode = new HashCodeBuilder().append(personalityName).append(migrationPossible).append(lastState).append(direction).append(currentVersion).append(firstVersion).append(lastVersion).toHashCode();
        }
        return hashCode;
    }

    private transient String toString;

    @Override
    public String toString()
    {
        if (toString == null) {
            toString =
                new ToStringBuilder(this).append("personalityName", personalityName)
                    .append("migrationPossible", migrationPossible)
                    .append("lastState", lastState)
                    .append("direction", direction)
                    .append("currentVersion", currentVersion)
                    .append("firstVersion", firstVersion)
                    .append("lastVersion", lastVersion)
                    .toString();
        }
        return toString;
    }



}


