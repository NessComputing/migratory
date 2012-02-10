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

import org.skife.jdbi.v2.Handle;


public abstract class Migration
{
    private final MigrationType migrationType;

    private final String personalityName;

    private final String scriptName;

    protected Migration(final MigrationType migrationType, final String personalityName, final String scriptName)
    {
        this.migrationType = migrationType;
        this.personalityName = personalityName;
        this.scriptName = scriptName;
    }

    public final String getPersonalityName()
    {
        return personalityName;
    }

    public final MigrationType getType()
    {
        return migrationType;
    }

    public final String getScriptName()
    {
        return scriptName;
    }


    public abstract int getStartVersion();

    public abstract int getEndVersion();

    public abstract String getDescription();

    public abstract String getLocation();

    public abstract String getChecksum();

    public abstract void migrate(final Handle handle);

    public abstract boolean isNeedsRoot();
}
