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
package com.nesscomputing.migratory;


import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.config.SimplePropertyConfigSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryException.Reason;
import com.nesscomputing.migratory.dbsupport.DbSupport;
import com.nesscomputing.migratory.dbsupport.DbSupportFactory;
import com.nesscomputing.migratory.information.DefaultMigrationInformationStrategy;
import com.nesscomputing.migratory.information.MigrationInformationStrategy;
import com.nesscomputing.migratory.jdbi.MigratoryDBI;
import com.nesscomputing.migratory.loader.ClasspathLoader;
import com.nesscomputing.migratory.loader.FileLoader;
import com.nesscomputing.migratory.loader.HttpLoader;
import com.nesscomputing.migratory.loader.JarLoader;
import com.nesscomputing.migratory.loader.LoaderManager;
import com.nesscomputing.migratory.loader.MetadataLoader;
import com.nesscomputing.migratory.loader.MigrationLoader;
import com.nesscomputing.migratory.locator.MetadataMigrationLocator;
import com.nesscomputing.migratory.locator.MigrationLocator;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.metadata.MetadataManager;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.validation.ValidationResult;


/**
 * Main facade. Application code should only deal with this class.
 */
public class Migratory implements MigratoryContext
{
    private static final Log LOG = Log.findLog();

    private final MigratoryConfig migratoryConfig;
    private final MigratoryDBI dbi;
    private final MigratoryDBI rootDbi;

    private final List<MigrationLocator> locators = Lists.newArrayList();
    private final LoaderManager loaderManager = new LoaderManager();
    private final Map<String, Object> defines = Maps.newHashMap();

    private MigrationInformationStrategy informationStrategy = new DefaultMigrationInformationStrategy();
    private DbSupportFactory dbSupportFactory = new DbSupportFactory();

    private volatile DbSupport dbSupport = null;
    private boolean initialized = false;

    public Migratory(final MigratoryConfig migratoryConfig, final IDBI dbi)
    {
        this.migratoryConfig = migratoryConfig;
        this.dbi = new MigratoryDBI(dbi);
        this.rootDbi = this.dbi;
    }

    public Migratory(final MigratoryConfig migratoryConfig, final IDBI dbi, final IDBI rootDbi)
    {
        this.migratoryConfig = migratoryConfig;
        this.dbi = new MigratoryDBI(dbi);
        this.rootDbi = new MigratoryDBI(rootDbi);
    }

    public Migratory(final MigratoryConfig migratoryConfig, final MigratoryDBIConfig dbiConfig, final MigratoryDBIConfig rootDbiConfig)
    throws MigratoryException
    {
        this.migratoryConfig = migratoryConfig;
        this.dbi = new MigratoryDBI(createDBI(dbiConfig));
        this.rootDbi = new MigratoryDBI(createDBI(rootDbiConfig));
    }

    public Migratory(final MigratoryConfig migratoryConfig, final MigratoryDBIConfig dbiConfig)
    throws MigratoryException
    {
        this.migratoryConfig = migratoryConfig;
        this.dbi = new MigratoryDBI(createDBI(dbiConfig));
        this.rootDbi = this.dbi;
    }

    public Migratory(final Properties properties)
    throws MigratoryException
    {
        final ConfigurationObjectFactory factory = new ConfigurationObjectFactory(new SimplePropertyConfigSource(properties));

        this.migratoryConfig = factory.build(MigratoryConfig.class);
        this.dbi = new MigratoryDBI(createDBI(factory.buildWithReplacements(MigratoryDBIConfig.class, ImmutableMap.of("_migratory", "migratory."))));
        this.rootDbi = this.dbi;
    }

    protected void init()
    {
        if (!initialized) {
            // This must be run before the init flag is set true!
            addLocator(new MetadataMigrationLocator(this));
            loaderManager.addLoader(new MetadataLoader(loaderManager));

            final Charset charset = Charset.forName(migratoryConfig.getEncoding());
            loaderManager.addLoader(new FileLoader(charset));
            loaderManager.addLoader(new JarLoader(charset));
            loaderManager.addLoader(new ClasspathLoader(loaderManager));
            loaderManager.addLoader(new HttpLoader(migratoryConfig));

            this.dbSupport = dbSupportFactory.getDbSupport(dbi);

            initialized = true;

            initDbi(dbi);

            if (dbi != rootDbi) {
                initDbi(rootDbi);
            }
        }
    }

    private void initDbi(final MigratoryDBI dbi)
    {
        for (Map.Entry<String, Object> define : defines.entrySet()) {
            dbi.addDefine(define.getKey(), define.getValue());
        }

        // This must be run after the init flag is set true!
        dbi.addDefine("db_type", getDbSupport().getDatabaseType());
        dbi.addDefine("table_name", migratoryConfig.getMetadataTableName());
        dbi.addDefine("metadata_name", MetadataManager.METADATA_MIGRATION_NAME);
        dbi.addDefine("state_ok", MigrationState.OK);
    }


    /**
     * Bring the current database to the requested levels.
     */
    public Map<String, List<MetadataInfo>> dbMigrate(final MigrationPlan migrationPlan, final MigratoryOption ... options) throws MigratoryException
    {
        init();
        final InternalMigrator migrator = new InternalMigrator(this);
        return migrator.migrate(migrationPlan, options);
    }

    /**
     * Run validation on the database. Make sure that all the changes recorded in the database are the same as the ones that should be applied.
     */
    public Map<String, ValidationResult> dbValidate(final Collection<String> personalities, final MigratoryOption ... options) throws MigratoryException
    {
        init();
        final InternalValidator validator = new InternalValidator(this);
        return validator.validate(personalities, options);
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the current schema.
     */
    public void dbClean(final MigratoryOption ... options)
    {
        init();
        final InternalClean internalClean = new InternalClean(this);
        internalClean.clean(options);
    }

    /**
     * Returns the status (current version) of the database.
     */
    public Map<String, StatusResult> dbStatus(final Collection<String> personalities, final MigratoryOption ... options)
    {
        init();
        final InternalStatus internalStatus = new InternalStatus(this);
        return internalStatus.status(personalities, options);
    }


    /**
     * Returns the full history of all applied migrations.
     */
        public Map<String, List<MetadataInfo>> dbHistory(final Collection<String> personalities, final MigratoryOption ... options)
            throws MigratoryException
    {
        init();
        final InternalHistory internalHistory = new InternalHistory(this);
        return internalHistory.history(personalities, options);
    }

    /**
     * Creates and initializes the Migratory metadata table.
     *
     * Returns null if the table already exists, returns a list of migration results
     * for the migrations executed otherwise.
     */
    public List<MetadataInfo> dbInit(final MigratoryOption ... options) throws MigratoryException
    {
        init();
        final InternalInit internalInit = new InternalInit(this);
        return internalInit.init(options);
    }

    @Override
    public MigratoryDBI getDBI()
    {
        return dbi;
    }

    @Override
    public MigratoryDBI getRootDBI()
    {
        return rootDbi;
    }

    @Override
    public MigratoryConfig getConfig()
    {
        return migratoryConfig;
    }

    @Override
    public Collection<MigrationLocator> getLocators()
    {
        return locators;
    }

    @Override
    public MigrationInformationStrategy getInformationStrategy()
    {
        return informationStrategy;
    }

    @Override
    public DbSupport getDbSupport()
    {
        init();
        return dbSupport;
    }

    public LoaderManager getLoaderManager()
    {
        return loaderManager;
    }

    public Migratory addLocator(final MigrationLocator locator)
    {
        if (initialized) {
            throw new MigratoryException(Reason.INIT, "Already initialized!");
        }

        locators.add(locator);
        LOG.debug("Added %s as migration locator.", locator);
        return this;
    }

    public Migratory addLoader(final MigrationLoader loader)
    {
        if (initialized) {
            throw new MigratoryException(Reason.INIT, "Already initialized!");
        }

        loaderManager.addLoader(loader);
        LOG.debug("Added %s as migration loader.", loader);
        return this;
    }

    public Migratory addDefine(final String key, final Object value)
    {
        defines.put(key, value);
        return this;
    }

    public Migratory setInformationStategy(final MigrationInformationStrategy informationStrategy)
    {
        if (initialized) {
            throw new MigratoryException(Reason.INIT, "Already initialized!");
        }

        this.informationStrategy = informationStrategy;
        return this;
    }

    /**
     * Add support for an additional database type. The dbName is *not* the canonical name used but the
     * name returned from the database engine on the JDBC metadata!
     */
    public Migratory addDbSupport(final String dbName, final Class<? extends DbSupport> dbSupport)
    {
        if (initialized) {
            throw new MigratoryException(Reason.INIT, "Already initialized!");
        }
        dbSupportFactory.addDbSupport(dbName, dbSupport);
        return this;
    }

    protected DBI createDBI(final MigratoryDBIConfig dbiConfig) throws MigratoryException
    {
        final String driver = dbiConfig.getDBDriverClass();

        // Force the driver to be loaded.
        if (StringUtils.isNotBlank(driver)) {
            try {
                Class.forName(driver).newInstance();
                LOG.debug("Loaded Driver '%s' successfully.", driver);
            }
            catch (ClassNotFoundException cnfe) {
                throw new MigratoryException(Reason.INIT, cnfe);
            }
            catch (InstantiationException ie) {
                throw new MigratoryException(Reason.INIT, ie);
            }
            catch (IllegalAccessException iae) {
                throw new MigratoryException(Reason.INIT, iae);
            }
        }

        LOG.debug("Loading DBI, URL: %s, User: %s, Password: %s", dbiConfig.getDBUrl(),
                                                                  dbiConfig.getDBUser(),
                                                                  (dbiConfig.isRevealPassword() ? dbiConfig.getDBPassword() : "XXXXX"));

        return new DBI (dbiConfig.getDBUrl(), dbiConfig.getDBUser(), dbiConfig.getDBPassword());
    }
}
