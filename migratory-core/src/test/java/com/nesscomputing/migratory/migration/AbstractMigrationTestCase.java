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
package com.nesscomputing.migratory.migration;


import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.migratory.AbstractMigratoryTest;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.MigratoryOption;
import com.nesscomputing.migratory.StatusResult;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.support.TestClasspathLocator;
import com.nesscomputing.migratory.support.TestFileLocator;
import com.nesscomputing.migratory.validation.ValidationResult;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationResultProblem;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationStatus;

/**
 * Test to demonstrate the migration functionality.
 */
public abstract class AbstractMigrationTestCase extends AbstractMigratoryTest
{
    public static final String PERSONALITY_NAME = "test";

    protected abstract String getTestName();

    @Override
    protected void init()
    {
        migratory.addLocator(new TestClasspathLocator(migratory, getTestName()));
    }

    @Test
    public void testMigrate() throws Exception
    {
        migrateAndCheckResult(migratory, new MigrationPlan(PERSONALITY_NAME), PERSONALITY_NAME, 1, 3, 3);
    }

    @Test
    public void testDoubleMigration() throws Exception
    {
        testMigrate();

        migrateAndCheckResult(migratory, new MigrationPlan(PERSONALITY_NAME), PERSONALITY_NAME, 0, 3, 3);
    }

    @Test
    public void testOneByOne() throws Exception
    {
        MigrationPlan step = new MigrationPlan().addMigration(PERSONALITY_NAME, 1);
        migrateAndCheckResult(migratory, step, PERSONALITY_NAME, 1, 1, 1);

        step = new MigrationPlan().addMigration(PERSONALITY_NAME, 2);
        migrateAndCheckResult(migratory, step, PERSONALITY_NAME, 1, 1, 2);

        step = new MigrationPlan().addMigration(PERSONALITY_NAME, 3);
        migrateAndCheckResult(migratory, step, PERSONALITY_NAME, 1, 1, 3);
    }

    @Test
    public void testValidateFails() throws Exception
    {
        final File tmpDir = Files.createTempDir();

        try {
            for (int i = 1 ; i < 4; i++) {
                final String file = "/test.00" + i + ".sql";
                Files.copy(Resources.newReaderSupplier(Resources.getResource(this.getClass(), "/test/migration" + file), Charsets.UTF_8), new File(tmpDir + file), Charsets.UTF_8);
            }

            migratory = new Migratory(new MigratoryConfig() {}, dbi);
            migratory.addLocator(new TestFileLocator(migratory, tmpDir));

            testMigrate();

            Files.append("tampering with the file", new File(tmpDir + "/test.002.sql"), Charsets.UTF_8);

            Map<String, ValidationResult> validationResult = migratory.dbValidate(null);

            Assert.assertNotNull(validationResult);

            final ValidationResult testResult = validationResult.get("test");
            Assert.assertNotNull(testResult);

            Assert.assertEquals(ValidationStatus.BAD, testResult.getValidationStatus());

            final List<ValidationResultProblem> problems = testResult.getProblems();
            Assert.assertNotNull(problems);
            Assert.assertEquals(1, problems.size());

            final ValidationResultProblem problem = problems.get(0);
            Assert.assertEquals(ValidationStatus.BAD_CHECKSUM, problem.getValidationStatus());
        }
        finally {
            FileUtils.deleteDirectory(tmpDir);
        }
    }

    @Test
    public void testValidateOk() throws Exception
    {
        final File tmpDir = Files.createTempDir();

        try {
            for (int i = 1 ; i < 4; i++) {
                final String file = "/test.00" + i + ".sql";
                Files.copy(Resources.newReaderSupplier(Resources.getResource(this.getClass(), "/test/migration" + file), Charsets.UTF_8), new File(tmpDir + file), Charsets.UTF_8);
            }

            migratory = new Migratory(new MigratoryConfig() {}, dbi);
            migratory.addLocator(new TestFileLocator(migratory, tmpDir));

            testMigrate();

            Map<String, ValidationResult> validationResult = migratory.dbValidate(null);

            Assert.assertNotNull(validationResult);

            final ValidationResult testResult = validationResult.get("test");
            Assert.assertNotNull(testResult);

            Assert.assertEquals(ValidationStatus.OK, testResult.getValidationStatus());

            final List<ValidationResultProblem> problems = testResult.getProblems();
            Assert.assertNotNull(problems);
            Assert.assertEquals(0, problems.size());
        }
        finally {
            FileUtils.deleteDirectory(tmpDir);
        }
    }


    @Test
    public void testMigrationFails() throws Exception
    {
        migratory = new Migratory(new MigratoryConfig() {}, dbi);
        init();
        migratory.addLocator(new TestClasspathLocator(migratory, "migration-fails"));

        Map<String, List<MetadataInfo>> result = migratory.dbMigrate(new MigrationPlan(PERSONALITY_NAME));

        Assert.assertEquals(1, result.size());
        if (result.size() > 0) {
            List<MetadataInfo> testResult = result.get("test");
            Assert.assertNotNull(testResult);
            Assert.assertEquals(4, testResult.size());
        }

        Map<String, StatusResult> status = migratory.dbStatus(null);
        StatusResult testStatus = status.get("test");

        Assert.assertNotNull(testStatus);
        Assert.assertEquals(3, testStatus.getCurrentVersion());
        Assert.assertEquals(MigrationState.OK, testStatus.getLastState());

        status = migratory.dbStatus(null, MigratoryOption.INCLUDE_FAILED);
        testStatus = status.get("test");

        Assert.assertNotNull(testStatus);
        Assert.assertEquals(4, testStatus.getCurrentVersion());
        Assert.assertEquals(MigrationState.FAIL, testStatus.getLastState());

        Map<String, List<MetadataInfo>> history = migratory.dbHistory(null);
        List<MetadataInfo> testHistory = history.get("test");

        Assert.assertNotNull(testHistory);
        Assert.assertEquals(4, testHistory.size());

        for (MetadataInfo info : testHistory) {
            assertChecksum(migratory, info, "test");
        }
    }

    @Test
    public void testTableExists() throws Exception
    {
        migratory.dbInit();
        Assert.assertTrue(tableExists(migratory.getConfig().getMetadataTableName()));
    }

    @Test
    public void testColumnExists() throws Exception
    {
        migratory.dbInit();
        Assert.assertTrue(columnExists(migratory.getConfig().getMetadataTableName(), "end_version"));
        Assert.assertTrue(columnExists(migratory.getConfig().getMetadataTableName(), "metadata_id"));
        Assert.assertFalse(columnExists(migratory.getConfig().getMetadataTableName(), "some_random_column"));
    }


//     protected void update(final String query)
//     {
//         getDBI().withHandle(new HandleCallback<Void>() {
//             @Override
//             public Void withHandle(final Handle handle) throws SQLException {
//                 handle.createStatement(query).execute();
//                 return null;
//             }
//          });
//     }
}
