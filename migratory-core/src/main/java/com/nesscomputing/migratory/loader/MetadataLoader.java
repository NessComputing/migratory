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
package com.nesscomputing.migratory.loader;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

/**
 * Loads all the metadata migration files from the class path. Metadata is requested
 * as metadata:<path> from loadFolder. The resulting URIs can be loaded with the
 */
public class MetadataLoader implements MigrationLoader
{
    private final LoaderManager loaderManager;

    public MetadataLoader(final LoaderManager loaderManager)
    {
        this.loaderManager = loaderManager;
    }

    @Override
    public boolean accept(final URI uri)
    {
        return (uri != null) && "metadata".equals(uri.getScheme());
    }

    @Override
    public Collection<URI> loadFolder(final URI folderUri, final String pattern)
    {
        try {
            final URI uriLocation = Resources.getResource(this.getClass(), folderUri.getPath()).toURI();
            return loaderManager.loadFolder(uriLocation, pattern);
        }
        catch (URISyntaxException e) {
            throw new MigratoryException(Reason.INTERNAL, e);
        }
    }

    /**
     * This method loads a file from a metadata:/... URI. This is probably not what you are looking for.
     */
    @Override
    public String loadFile(final URI fileUri)
    {
        try {
            final URL urlLocation = Resources.getResource(this.getClass(), fileUri.getPath());
            return Resources.toString(urlLocation, Charsets.UTF_8);
        }
        catch (IOException e) {
            throw new MigratoryException(Reason.INTERNAL, e);
        }
    }
}
