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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.dbsupport.h2.H2DbSupport;
import com.nesscomputing.migratory.dbsupport.postgresql.PostgreSQLDbSupport;


/**
 * Factory for obtaining the correct DbSupport instance for the current connection.
 */
public class DbSupportFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DbSupportFactory.class);

    private final Map<String, Class<? extends DbSupport>> supportedDatabases;

    public DbSupportFactory()
    {
        supportedDatabases = Maps.newHashMap();

        supportedDatabases.put("H2", H2DbSupport.class);
        supportedDatabases.put("PostgreSQL", PostgreSQLDbSupport.class);
    }

    public DbSupport getDbSupport(final IDBI dbi)
        throws MigratoryException
    {
        final String databaseProductname = dbi.withHandle(new HandleCallback<String>() {
           @Override
           public String withHandle(final Handle handle) throws SQLException, MigratoryException {

               final DatabaseMetaData databaseMetaData = handle.getConnection().getMetaData();
               if (databaseMetaData == null) {
                   throw new MigratoryException(Reason.DATABASE, "Unable to read database metadata while it is null!");
               }
               return databaseMetaData.getDatabaseProductName();
           }
        });

        final Class<? extends DbSupport> dbSupportClazz = supportedDatabases.get(databaseProductname);
        if (dbSupportClazz == null) {
            throw new MigratoryException(Reason.DATABASE, "Database %s is not supported!", databaseProductname);
        }

        LOG.trace("Retrieved {} for {}", dbSupportClazz.getCanonicalName(), databaseProductname);

        try {
            final Constructor<? extends DbSupport> c = dbSupportClazz.getConstructor(IDBI.class);
            return c.newInstance(dbi);
        }
        catch (SecurityException se) {
            throw new MigratoryException(Reason.INTERNAL, se);
        }
        catch (NoSuchMethodException nsme) {
            throw new MigratoryException(Reason.INTERNAL, nsme);
        }
        catch (IllegalArgumentException iae) {
            throw new MigratoryException(Reason.INTERNAL, iae);
        }
        catch (InstantiationException ie) {
            throw new MigratoryException(Reason.INTERNAL, ie);
        }
        catch (IllegalAccessException iae) {
            throw new MigratoryException(Reason.INTERNAL, iae);
        }
        catch (InvocationTargetException ite) {
            throw new MigratoryException(Reason.INTERNAL, ite.getTargetException());
        }
    }

    public void addDbSupport(final String dbName, final Class<? extends DbSupport> dbSupportClazz)
    {
        supportedDatabases.put(dbName, dbSupportClazz);
        LOG.debug("Registered {} for {}", dbSupportClazz.getCanonicalName(), dbName);
    }
}
