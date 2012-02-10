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
package com.nesscomputing.migratory.dbsupport.h2;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;

import com.google.common.collect.ImmutableList;
import com.nesscomputing.migratory.dbsupport.DbSupport;
import com.nesscomputing.migratory.migration.sql.SqlScript;
import com.nesscomputing.migratory.migration.sql.SqlStatement;


/**
 * H2 database specific support
 */
public class H2DbSupport implements DbSupport
{
    public static final String H2_TEMPLATE_PREFIX = "#h2_support:";

    private final IDBI dbi;

    /**
     * Creates a new instance.
     */
    public H2DbSupport(final IDBI dbi)
    {
        this.dbi = dbi;
    }

    @Override
    public String getDatabaseType()
    {
        return "h2";
    }

    @Override
    public String getCurrentSchema(final Handle handle) throws SQLException
    {
        final ResultSet resultSet = handle.getConnection().getMetaData().getSchemas();
        try {
            while (resultSet.next()) {
                if (resultSet.getBoolean("IS_DEFAULT")) {
                    return resultSet.getString("TABLE_SCHEM");
                }
            }
            return null;
        }
        finally {
            resultSet.close();
        }
    }

    @Override
    public boolean tableExists(final String table)
    {
        return dbi.withHandle(new HandleCallback<Boolean>() {
            @Override
            public Boolean withHandle(final Handle handle) throws SQLException {
                final ResultSet resultSet = handle.getConnection().getMetaData().getTables(null, getCurrentSchema(handle), table.toUpperCase(Locale.ENGLISH), null);
                try {
                    return resultSet.next();
                }
                finally {
                    resultSet.close();
                }
            }
        });
    }

    @Override
    public boolean columnExists(final String table, final String column)
    {
        return dbi.withHandle(new HandleCallback<Boolean>() {
            @Override
            public Boolean withHandle(final Handle handle) throws SQLException {
                final ResultSet resultSet = handle.getConnection().getMetaData().getColumns(null, getCurrentSchema(handle), table.toUpperCase(Locale.ENGLISH), column.toUpperCase(Locale.ENGLISH));
                try {
                    return resultSet.next();
                }
                finally {
                    resultSet.close();
                }
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript sqlScriptFor(final String sqlScriptSource)
    {
        return new SqlScript(sqlScriptSource);
    }

    @Override
    public SqlScript createCleanScript(final Handle handle)  throws SQLException
    {
        return new SqlScript(ImmutableList.of(new SqlStatement(0, H2_TEMPLATE_PREFIX + "drop_all_objects")));
    }
}
