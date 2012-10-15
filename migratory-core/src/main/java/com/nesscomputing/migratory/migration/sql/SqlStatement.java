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
package com.nesscomputing.migratory.migration.sql;

import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.builder.ToStringBuilder;



/**
 * A sql statement from a script that can be executed at once against a database.
 */
public class SqlStatement
{
    private final int count;

    private final String sql;

    private final Map<String, Object> defines = Maps.newHashMap();

    public SqlStatement(final int count, final String sql)
    {
        this.count = count;
        this.sql = sql;
    }

    public void addDefine(final String key, final Object value)
    {
        this.defines.put(key, value);
    }

    public Map<String, Object> getDefines()
    {
        return defines;
    }

    public int getCount()
    {
        return count;
    }

    public String getSql()
    {
        return sql;
    }

    private transient String toString;

    @Override
    public String toString()
    {
        if (toString == null) {
            toString = new ToStringBuilder(this).append("count", count).append("sql", sql).append("defines", defines).toString();
        }
        return toString;
    }
}
