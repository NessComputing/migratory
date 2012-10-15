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

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class MigrationPlan
{
    private final Set<MigrationPlanEntry> planEntries = Sets.newTreeSet();

    public MigrationPlan(final String ... personalityNames)
    {
        if (personalityNames != null) {
            for (String personalityName : personalityNames) {
                addMigration(personalityName);
            }
        }
    }

    public MigrationPlan addMigration(final String personalityName)
    {
        addMigration(personalityName, Integer.MAX_VALUE, 0);
        return this;
    }

    public MigrationPlan addMigration(final String personalityName, final int targetVersion)
    {
        addMigration(personalityName, targetVersion, 0);
        return this;
    }

    public MigrationPlan addMigration(final String personalityName, final int targetVersion, final int priority)
    {
        planEntries.add(new MigrationPlanEntry(personalityName, targetVersion, priority));
        return this;
    }

    public boolean isEmpty()
    {
        return planEntries.isEmpty();
    }

    public int size()
    {
        return planEntries.size();
    }

    public Collection<MigrationPlanEntry> getEntries()
    {
        return planEntries;
    }

    public static class MigrationPlanEntry implements Comparable<MigrationPlanEntry>
    {
        private final int priority;
        private final String personalityName;
        private final int targetVersion;

        MigrationPlanEntry(final String personalityName, final int targetVersion, final int priority)
        {
            this.personalityName = personalityName;
            this.targetVersion = targetVersion;
            this.priority = priority;
        }

        public String getPersonalityName()
        {
            return personalityName;
        }


        public int getTargetVersion()
        {
            return targetVersion;
        }


        public int getPriority()
        {
            return priority;
        }


        private transient String toString;

        @Override
        public String toString()
        {
            if (toString == null) {
                toString = new ToStringBuilder(this).append("personalityName", personalityName).append("targetVersion", targetVersion).append("priority", priority).toString();
            }
            return toString;
        }


        public int compareTo(final MigrationPlanEntry other)
        {
            final int result = new CompareToBuilder().append(priority, other.priority).append(personalityName, other.personalityName).append(targetVersion, other.targetVersion).toComparison();
            // Higher priority needs to be run before lower priority. So revert the actual compare result for reverse ordering.
            return -result;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (!(other instanceof MigrationPlanEntry))
                return false;
            MigrationPlanEntry castOther = (MigrationPlanEntry) other;
            return new EqualsBuilder().append(priority, castOther.priority).append(personalityName, castOther.personalityName).append(targetVersion, castOther.targetVersion).isEquals();
        }


        private transient int hashCode;

        @Override
        public int hashCode()
        {
            if (hashCode == 0) {
                hashCode = new HashCodeBuilder().append(priority).append(personalityName).append(targetVersion).toHashCode();
            }
            return hashCode;
        }


    }
}
