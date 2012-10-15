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
package com.nesscomputing.migratory.mojo.database.util;

import java.net.URI;
import java.util.Map;

import com.google.common.collect.Maps;

import com.nesscomputing.migratory.MigratoryContext;
import com.nesscomputing.migratory.locator.AbstractSqlResourceLocator;

public class MojoLocator extends AbstractSqlResourceLocator
{
    private final String manifestUrl;

    public MojoLocator(final MigratoryContext migratoryContext, final String manifestUrl)
    {
        super(migratoryContext);
        this.manifestUrl = manifestUrl;
    }

    @Override
    protected Map.Entry<URI, String> getBaseInformation(final String personalityName, final String databaseType)
    {
        final StringBuilder location = new StringBuilder(manifestUrl);
        if (!manifestUrl.endsWith("/")) {
            location.append("/");
        }
        location.append(personalityName);

        return Maps.immutableEntry(URI.create(location.toString()), personalityName + ".*");
    }
}
