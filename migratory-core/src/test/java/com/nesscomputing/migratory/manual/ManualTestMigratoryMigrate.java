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
package com.nesscomputing.migratory.manual;


import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.nesscomputing.migratory.AbstractMigratoryTest;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationPlan;
import com.nesscomputing.migratory.migration.MigrationPlan.MigrationPlanEntry;
import com.nesscomputing.migratory.support.TestClasspathLocator;
import com.nesscomputing.migratory.validation.ValidationResult;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationStatus;

public class ManualTestMigratoryMigrate extends AbstractMigratoryTest
{
    @Override
    protected DBI getDBI() throws Exception
    {
        return new DBI("jdbc:postgresql://localhost/migratory_test", "postgres", "");
    }

    @Override
    public void init()
    {
        migratory.addLocator(new TestClasspathLocator(migratory, "postgresql/migration"));
        migratory.dbClean();
    }


    @After
    public void shutdownDb()
    {
        Assert.assertNotNull(dbi);

        final MigratoryConfig migratoryConfig = new MigratoryConfig() {};
        final Migratory migratory = new Migratory(migratoryConfig, dbi);
        migratory.dbClean();
    }


    @Test
    public void testInit()
    {
        final MigrationPlan planToExecute = new MigrationPlan()
        .addMigration("prereq", Integer.MAX_VALUE, 1)
        .addMigration("p1")
        .addMigration("p2");;

        final Map<String, List<MetadataInfo>> results = migratory.dbMigrate(planToExecute);

        Assert.assertEquals(planToExecute.size(), results.size());

        for (MigrationPlanEntry entry : planToExecute.getEntries()) {
            Assert.assertNotNull(results.get(entry.getPersonalityName()));
        }

        validateDb();
    }

    @Test
    public void testDoubleStep()
    {
        MigrationPlan planToExecute = new MigrationPlan()
            .addMigration("prereq", Integer.MAX_VALUE, 1)
            .addMigration("p1", 1)
            .addMigration("p2", 1);

        Map<String, List<MetadataInfo>> results = migratory.dbMigrate(planToExecute);

        Assert.assertEquals(planToExecute.size(), results.size());

        for (MigrationPlanEntry entry : planToExecute.getEntries()) {
            Assert.assertNotNull(results.get(entry.getPersonalityName()));
        }

        validateDb();

        planToExecute = new MigrationPlan()
        .addMigration("p1");

        results = migratory.dbMigrate(planToExecute);

        Assert.assertEquals(planToExecute.size(), results.size());

        for (MigrationPlanEntry entry : planToExecute.getEntries()) {
            Assert.assertNotNull(results.get(entry.getPersonalityName()));
        }

        validateDb();
    }

    @Test
    public void testSingleStep()
    {
        MigrationPlan planToExecute = new MigrationPlan("p2")
        .addMigration("prereq", Integer.MAX_VALUE, 1);

        migratory.dbMigrate(planToExecute);

        for (int count = 1; count < 8; count++) {

            planToExecute = new MigrationPlan()
            .addMigration("p1", count);

            migratory.dbMigrate(planToExecute);

            validateDb();
        };

        planToExecute = new MigrationPlan("p1");
        Map<String, List<MetadataInfo>> results = migratory.dbMigrate(planToExecute);
        Assert.assertTrue(results.isEmpty());
    }


    private void validateDb()
    {
        final Map<String, ValidationResult> validations = migratory.dbValidate(null);
        Assert.assertEquals(3, validations.size());
        for (ValidationResult validation : validations.values()) {
            Assert.assertEquals(ValidationStatus.OK, validation.getValidationStatus());
        }
    }
}

