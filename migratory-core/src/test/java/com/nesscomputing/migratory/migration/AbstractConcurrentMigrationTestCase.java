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

import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.migratory.AbstractMigratoryTest;
import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.migratory.StatusResult;
import com.nesscomputing.migratory.metadata.MetadataInfo;
import com.nesscomputing.migratory.migration.MigrationResult.MigrationState;
import com.nesscomputing.migratory.support.TestClasspathLocator;


/**
 * Test to demonstrate the migration functionality using H2.
 */
public abstract class AbstractConcurrentMigrationTestCase extends AbstractMigratoryTest
{
	private static final int NUM_THREADS = 10;

    public static final String PERSONALITY_NAME = "test";

	private boolean failed;

    @Override
    protected void init()
    {
        migratory.addLocator(new TestClasspathLocator(migratory, getTestName()));
    }

    public String getTestName()
    {
        return "migration";
    }

	@Test
	public void migrateConcurrently() throws Exception
    {
        // Can't be run concurrently
        migratory.dbInit();

		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
                    Migratory threadMigratory = new Migratory(new MigratoryConfig() {}, dbi);
                    threadMigratory.addLocator(new TestClasspathLocator(threadMigratory, getTestName()));
                    threadMigratory.dbMigrate(new MigrationPlan(PERSONALITY_NAME));
				} catch (Exception e) {
					e.printStackTrace();
					failed = true;
				}
			}
		};

		Thread[] threads = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(runnable);
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i].start();
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i].join();
		}

		assertFalse(failed);

        Map<String, StatusResult> status = migratory.dbStatus(null);
        StatusResult testStatus = status.get(PERSONALITY_NAME);

        Assert.assertNotNull(testStatus);
        Assert.assertEquals(3, testStatus.getCurrentVersion());
        Assert.assertEquals(MigrationState.OK, testStatus.getLastState());

        Map<String, List<MetadataInfo>> history = migratory.dbHistory(null);
        List<MetadataInfo> testHistory = history.get(PERSONALITY_NAME);

        Assert.assertNotNull(testHistory);
        Assert.assertEquals(3, testHistory.size());

        for (MetadataInfo info : testHistory) {
            assertChecksum(migratory, info, PERSONALITY_NAME);
        }
	}
}
