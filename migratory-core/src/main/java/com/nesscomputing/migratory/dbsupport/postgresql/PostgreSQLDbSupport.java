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
package com.nesscomputing.migratory.dbsupport.postgresql;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.dbsupport.DbSupport;
import com.nesscomputing.migratory.migration.sql.SqlScript;
import com.nesscomputing.migratory.migration.sql.SqlStatement;

/**
 * PostgreSQL-specific support.
 */
public class PostgreSQLDbSupport implements DbSupport
{
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDbSupport.class);

    private final static String[] TABLE_EXISTS_TABLE_TYPES = new String[]{"TABLE"};

    public static final String POSTGRES_TEMPLATE_PREFIX = "#postgresql_support:";

    private static final String [] OBJECT_TYPES = new String[] {
        "triggers", "tables", "views", "seqs", "functions", "types"
    };

    private final IDBI dbi;

    /**
     * Creates a new instance.
     */
    public PostgreSQLDbSupport(final IDBI dbi)
    {
        this.dbi = dbi;
    }


    @Override
    public String getDatabaseType()
    {
        return "postgresql";
    }

    @Override
    public String getCurrentSchema(final Handle handle) throws SQLException
    {
        return handle.createQuery(POSTGRES_TEMPLATE_PREFIX + "current_schema").map(StringMapper.FIRST).first();
    }

    @Override
    public boolean tableExists(final String table)
    {
        return dbi.withHandle(new HandleCallback<Boolean>() {
            @Override
            public Boolean withHandle(final Handle handle) throws SQLException {
                ResultSet resultSet = handle.getConnection().getMetaData().getTables(null, getCurrentSchema(handle), table.toLowerCase(), TABLE_EXISTS_TABLE_TYPES);
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
                ResultSet resultSet = handle.getConnection().getMetaData().getColumns(null, getCurrentSchema(handle), table.toLowerCase(), column.toLowerCase());
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
        return true;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript sqlScriptFor(final String sqlScriptSource)
    {
        return new PostgreSQLSqlScript(sqlScriptSource);
    }

    @Override
    public SqlScript createCleanScript(final Handle handle) throws SQLException
    {
        final List<SqlStatement> sqlStatements = Lists.newArrayList();

        for (final String objectType : OBJECT_TYPES) {
            final List<Map<String, Object>> objectInfo =
                handle.createQuery(POSTGRES_TEMPLATE_PREFIX + "find_" + objectType).list();

            for (Map<String, Object> info : objectInfo) {
                final SqlStatement sqlStatement = new SqlStatement(0, POSTGRES_TEMPLATE_PREFIX + "drop_" + objectType);
                for (Map.Entry<String, Object> entry : info.entrySet()) {
                    sqlStatement.addDefine(entry.getKey(), entry.getValue());
                }
                sqlStatements.add(sqlStatement);
                LOG.trace("adding SQL Statement: {}", sqlStatement);
            }
        }

        return new SqlScript(sqlStatements);
    }
}
