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
package com.nesscomputing.migratory.information;

public class MigrationInformation
{
    private final String personalityName;
    private final int startVersion;
    private final int endVersion;
    private final boolean needsRoot;
    private final boolean template;

    public MigrationInformation(final String personalityName, final int startVersion, final int endVersion, final boolean needsRoot, final boolean template)
    {
        this.personalityName = personalityName;
        this.startVersion = startVersion;
        this.endVersion = endVersion;
        this.needsRoot = needsRoot;
        this.template = template;
    }

    public String getPersonalityName()
    {
        return personalityName;
    }

    public int getStartVersion()
    {
        return startVersion;
    }

    public int getEndVersion()
    {
        return endVersion;
    }

    public boolean isNeedsRoot()
    {
        return needsRoot;
    }

    public boolean isTemplate()
    {
        return template;
    }
}
