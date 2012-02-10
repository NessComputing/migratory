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
package com.nesscomputing.migratory.jdbi;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.joda.time.DateTime;

public final class JdbiMappers
{
    private JdbiMappers()
    {
    }

    /**
     * Returns a DateTime object representing the date or null if the input is null.
     */
    public static DateTime getDateTime(final ResultSet rs, final String columnName) throws SQLException
    {
        final Timestamp ts = rs.getTimestamp(columnName);

        return (ts == null) ? null : new DateTime(ts);
    }

    /**
     * Returns an Enum representing the data or null if the input is null.
     */
    public static <T extends Enum<T>> T getEnum(final ResultSet rs, final Class<T> enumType, final String columnName) throws SQLException
    {
        final String str = rs.getString(columnName);

        return (str == null) ? null : Enum.valueOf(enumType, str);
    }

    /**
     * Returns an URI representing the data or null if the input is null.
     */
    public static URI getURI(final ResultSet rs, final String columnName) throws SQLException
    {
        final String str = rs.getString(columnName);

        return (str == null) ? null : URI.create(str);
    }
}
