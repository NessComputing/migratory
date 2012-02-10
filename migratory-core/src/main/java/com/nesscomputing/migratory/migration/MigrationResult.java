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
package com.nesscomputing.migratory.migration;

import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;


/**
 * Represents the result of a single migration.
 */
public class MigrationResult
{
    public enum MigrationState {
        OK, FAIL
    }

    private final MigrationState migrationState;
    private final long executionTime;
    private final Migration migration;
    private final MigrationDirection direction;

    public MigrationResult(final MigrationState migrationState, final MigrationDirection direction, final long executionTime, final Migration migration)
    {
        this.migrationState = migrationState;
        this.direction = direction;
        this.executionTime = executionTime;
        this.migration = migration;
    }

    public MigrationState getState()
    {
        return migrationState;
    }

    public MigrationDirection getDirection()
    {
        return direction;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public Migration getMigration()
    {
        return migration;
    }
}
