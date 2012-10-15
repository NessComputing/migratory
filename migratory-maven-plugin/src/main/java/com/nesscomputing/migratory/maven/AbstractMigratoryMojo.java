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
package com.nesscomputing.migratory.maven;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.MigratoryOption;

abstract class AbstractMigratoryMojo extends AbstractMojo
{
    private static final Log CONSOLE = Log.forName("console");

    protected MigratoryOption [] optionList;

    /**
     * @parameter expression="${encoding}"
     */
    private String encoding = "utf8";

    /**
     * @parameter expression="${metadata-table}"
     */
    private String metadataTable = "migratory_metadata";

    /**
     * @parameter expression="${readonly}"
     */
    private boolean readOnly = false;

    /**
     * @parameter expression="${create-personalities}"
     */
    private boolean createPersonalities = true;

    /**
     * @parameter expression="${allow-rollforward}"
     */
    private boolean allowRollforward = true;

    /**
     * @parameter expression="${allow-rollbackward}"
     */
    private boolean allowRollbackward = true;

    /**
     * @parameter expression="${db.driver}"
     */
    private String driver;

    /**
     * @parameter expression="${db.url}"
     * @required
     */
    private String url;

    /**
     * @parameter expression="${db.user}"
     */
    private String user;

    /**
     * @parameter expression="${db.password}"
     */
    private String password;

    /**
     * @parameter expression="${options}"
     */
    private String options;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        ConfigureLog4j.start(this);

        try {
            this.optionList = parseOptions(options);

            final Migratory migratory = new Migratory(getConfig(), createDBI());
            doExecute(migratory);
        }
        catch (Exception e) {
            CONSOLE.errorDebug(e, "While executing Mojo %s", this.getClass().getSimpleName());
            throw new MojoExecutionException("Migratory Error: ", e);
        }
        finally {
            ConfigureLog4j.stop(this);
        }
    }

    /**
     * Executes this mojo.
     */
    protected abstract void doExecute(Migratory migratory) throws Exception;

    /**
     * Creates the datasource config based on the provided parameters.
     */
    private DBI createDBI() throws Exception
    {
        if (driver != null) {
            Class.forName(driver).newInstance();
        }
        return new DBI(url, user, password);
    }

    private MigratoryConfig getConfig()
    {
        return new MigratoryConfig() {
            @Override
            public String getEncoding()
            {
                return encoding;
            }

            @Override
            public String getMetadataTableName()
            {
                return metadataTable;
            }

            @Override
            public boolean isReadOnly()
            {
                return readOnly;
            }

            @Override
            public boolean isCreatePersonalities()
            {
                return createPersonalities;
            }

            @Override
            public boolean isAllowRollForward()
            {
                return allowRollforward;
            }

            @Override
            public boolean isAllowRollBack()
            {
                return allowRollbackward;
            }
        };
    }

    protected MigratoryOption [] parseOptions(final String options)
    {
        final String [] optionList = StringUtils.stripAll(StringUtils.split(options, ","));

        if (optionList == null) {
            return new MigratoryOption[0];
        }

        final MigratoryOption [] migratoryOptions = new MigratoryOption[optionList.length];
        for (int i = 0 ; i < optionList.length; i++) {
            migratoryOptions[i] = MigratoryOption.valueOf(optionList[i].toUpperCase(Locale.ENGLISH));
        }

        CONSOLE.debug("Parsed %s into %s", options, migratoryOptions);
        return migratoryOptions;
    }

    protected List<String> parsePersonalities(final String personalityList)
    {
        final String [] personalities = StringUtils.stripAll(StringUtils.split(personalityList, ","));
        CONSOLE.debug("Found %s as personalities", ((Object []) personalities));
        return personalities == null ? null : Arrays.asList(personalities);
    }


}
