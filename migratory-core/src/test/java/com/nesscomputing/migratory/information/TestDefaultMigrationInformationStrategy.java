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
package com.nesscomputing.migratory.information;


import java.io.File;

import com.nesscomputing.migratory.MigratoryException;

import org.junit.Assert;
import org.junit.Test;

public class TestDefaultMigrationInformationStrategy
{
    private final MigrationInformationStrategy strategy = new DefaultMigrationInformationStrategy();

    @Test
    public void testSimple()
    {
        MigrationInformation information = strategy.getInformation(new File("/tmp/sometest.0001.sql").toURI());
        Assert.assertEquals("sometest", information.getPersonalityName());
        Assert.assertEquals(0, information.getStartVersion());
        Assert.assertEquals(1, information.getEndVersion());
        Assert.assertFalse(information.isTemplate());
    }

    @Test
    public void testTemplate()
    {
        MigrationInformation information = strategy.getInformation(new File("/tmp/sometest.0001.st").toURI());
        Assert.assertEquals("sometest", information.getPersonalityName());
        Assert.assertEquals(0, information.getStartVersion());
        Assert.assertEquals(1, information.getEndVersion());
        Assert.assertTrue(information.isTemplate());
    }

    @Test
    public void testOneSix()
    {
        MigrationInformation information = strategy.getInformation(new File("/tmp/sometest.0001-0006.sql").toURI());
        Assert.assertEquals("sometest", information.getPersonalityName());
        Assert.assertEquals(1, information.getStartVersion());
        Assert.assertEquals(6, information.getEndVersion());
        Assert.assertFalse(information.isTemplate());
    }

    @Test
    public void testBackwards()
    {
        MigrationInformation information = strategy.getInformation(new File("/tmp/sometest.0002-0001.sql").toURI());
        Assert.assertEquals("sometest", information.getPersonalityName());
        Assert.assertEquals(2, information.getStartVersion());
        Assert.assertEquals(1, information.getEndVersion());
        Assert.assertFalse(information.isTemplate());
    }

    @Test
    public void testNullOk()
    {
        Assert.assertNull(strategy.getInformation(null));
    }

    @Test(expected = MigratoryException.class)
    public void testNoText()
    {
        strategy.getInformation(new File("/tmp/0002-0001.sql").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testToZero()
    {
        strategy.getInformation(new File("/tmp/hello.0001-0000.sql").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testZero()
    {
        strategy.getInformation(new File("/tmp/hello.0000.sql").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testNoOp()
    {
        strategy.getInformation(new File("/tmp/sometest.2000-2000.sql").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testBadSuffix()
    {
        strategy.getInformation(new File("/tmp/sometest.2000-2001.oracle").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testStrangeFile()
    {
        strategy.getInformation(new File("/tmp/ich.bin.zwei.oeltanks.12345.sql").toURI());
    }

    @Test(expected = MigratoryException.class)
    public void testStrangeVersion()
    {
        strategy.getInformation(new File("/tmp/strangelove.4-8-15-16-23-42.sql").toURI());
    }
}
