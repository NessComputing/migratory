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

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.migratory.ImmutableMigratoryDBIConfig;
import com.nesscomputing.migratory.MigratoryDBIConfig;

public class TestImmutableMigratoryDBIConfig
{
    @Test
    public void testSimple()
    {
        final MigratoryDBIConfig cfg = new ImmutableMigratoryDBIConfig("jdbc:postgresql://localhost/test", "hello", "world");

        Assert.assertEquals("jdbc:postgresql://localhost/test", cfg.getDBUrl());
        Assert.assertEquals("hello", cfg.getDBUser());
        Assert.assertEquals("world", cfg.getDBPassword());
    }

    @Test(expected=IllegalArgumentException.class)
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void testNullUri()
    {
        new ImmutableMigratoryDBIConfig(null, "hello", "world");
    }
}

