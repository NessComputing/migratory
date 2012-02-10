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


import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.skife.jdbi.v2.Handle;

import com.google.common.base.Charsets;
import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.information.MigrationInformation;
import com.nesscomputing.migratory.jdbi.MigratoryDBI;
import com.nesscomputing.migratory.migration.Migration;
import com.nesscomputing.migratory.migration.MigrationType;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends Migration
{
    private static final MessageDigest SHA_1;

    static {
        try {
            SHA_1 = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new ExceptionInInitializerError(nsae);
        }
    }

    private final URI location;
    private final MigrationInformation migrationInformation;
    private final SqlScript sqlScript;
    private final String checksum;

    public SqlMigration(final MigratoryContext migratoryContext, final String personalityName, final URI location, final String scriptName, final String rawSql)
    {
        super(MigrationType.SQL, personalityName, scriptName);

        this.location = location;
        this.migrationInformation = migratoryContext.getInformationStrategy().getInformation(location);
        this.sqlScript = migratoryContext.getDbSupport().sqlScriptFor(rawSql);
        this.checksum = sha1For(rawSql);

        // Register the SQL scripts with the DBI loader
        for(final SqlStatement sqlStatement : sqlScript.getSqlStatements()) {
            final MigratoryDBI dbi = migrationInformation.isNeedsRoot() ? migratoryContext.getRootDBI() : migratoryContext.getDBI();
            dbi.addTemplate(getIdentifier(sqlStatement.getCount()), sqlStatement.getSql());
        }
    }

    @Override
    public String getLocation()
    {
        return location.toString();
    }

    @Override
    public int getStartVersion()
    {
        return migrationInformation.getStartVersion();
    }

    @Override
    public int getEndVersion()
    {
        return migrationInformation.getEndVersion();
    }

    @Override
    public String getDescription()
    {
        return "SQL script migration";
    }

    @Override
    public String getChecksum()
    {
        return checksum;
    }

    @Override
    public boolean isNeedsRoot()
    {
        return migrationInformation.isNeedsRoot();
    }

    @Override
    public void migrate(final Handle handle)
    {
        for (SqlStatement sqlStatement : sqlScript.getSqlStatements()) {
            handle.createStatement(getIdentifier(sqlStatement.getCount())).execute();
        }
    }

    public boolean isTemplate()
    {
        return migrationInformation.isTemplate();
    }

    private String getIdentifier(final int count)
    {
        return "@" + (isTemplate() ? "T" : "R") + count + "@" + location;
    }

    private String sha1For(String rawSql)
    {
        final byte[] data = rawSql.getBytes(Charsets.UTF_8);

        SHA_1.update(data, 0, data.length);
        byte[] sha1hash = SHA_1.digest();
        StringBuilder result = new StringBuilder();
        for (byte sha1byte : sha1hash) {
            result.append(Integer.toHexString(0xff & sha1byte));
        }

        return result.toString();
    }

    @Override
    public String toString()
    {
        return String.format("SQL Migration for '%s' from %d to %d (%s), loaded from %s", getPersonalityName(), getStartVersion(), getEndVersion(), isTemplate() ? "Template" : "Script", location);
    }

}
