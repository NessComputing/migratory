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
package com.nesscomputing.migratory.dbsupport;


import java.sql.SQLException;

import org.skife.jdbi.v2.Handle;

import com.nesscomputing.migratory.migration.sql.SqlScript;


/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Creates a new sql script from this resource with these placeholders to replace.
     */
    SqlScript sqlScriptFor(final String rawSql);

    /**
     * Creates a new sql script which clean the current schema, by dropping all objects.
     */
    SqlScript createCleanScript(final Handle handle) throws SQLException;

    /**
     * Returns the canonical name for the database type.
     */
    String getDatabaseType();

    /**
     * Checks whether this table is already present in the database.
     */
    boolean tableExists(String table);

    /**
     * Checks whether this column is already present in this table in the database.
     */
    boolean columnExists(String table, String column);

    /**
     * Retrieves the current schema.
     */
    String getCurrentSchema(final Handle handle) throws SQLException;

    /**
     * Checks whether ddl transactions are supported for this database.
     */
    boolean supportsDdlTransactions();

    /**
     * Checks whether locking using select ... for update is supported for this database.
     */
    boolean supportsLocking();
}
