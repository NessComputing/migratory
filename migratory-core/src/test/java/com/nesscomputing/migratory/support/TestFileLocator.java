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
package com.nesscomputing.migratory.support;


import java.io.File;
import java.net.URI;
import java.util.Map;

import com.google.common.collect.Maps;

import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.locator.AbstractSqlResourceLocator;

public class TestFileLocator extends AbstractSqlResourceLocator
{
    private final File folder;

    public TestFileLocator(final MigratoryContext migratoryContext, final File folder)
    {
        super(migratoryContext);
        this.folder = folder;
    }

    @Override
    protected Map.Entry<URI, String> getBaseInformation(final String personalityName, final String databaseType)
    {
        if (folder.exists() && folder.canRead()) {
            if (folder.isDirectory()) {
                final URI uriLocation = folder.toURI();
                return Maps.immutableEntry(uriLocation, personalityName + ".*");
            }
        }
        return null;
    }
}
