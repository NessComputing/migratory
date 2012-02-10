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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;


/**
 * JDBI converter to bind an Enum
 */
public class EnumArgument<T extends Enum<T>> implements Argument
{
    private final Enum<T> enumValue;

    EnumArgument(final Enum<T> enumValue)
    {
        this.enumValue = enumValue;
    }

    @Override
    public void apply(final int position,
                      final PreparedStatement statement,
                      final StatementContext ctx) throws SQLException
    {
        if (enumValue == null) {
            statement.setNull(position, Types.VARCHAR);
        }
        else {
            statement.setString(position, enumValue.toString());
        }
    }

    @Override
    public String toString()
    {
        return enumValue.toString();
    }
}
