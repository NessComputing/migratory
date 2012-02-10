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
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

/**
 * Loads a resource from a remote or local location.
 */
public class LoaderManager implements MigrationLoader
{
    private List<MigrationLoader> loaders = Lists.newArrayList();

    public void addLoader(MigrationLoader loader)
    {
        loaders.add(loader);
    }

    public List<MigrationLoader> getLoaders()
    {
        return loaders;
    }

    /**
     * Returns true if this loader accepts the URI.
     */
    public boolean accept(final URI uri)
    {
        for (final MigrationLoader loader : loaders) {
            if (loader.accept(uri)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads a collection of URIs from a given location. The resulting
     * URIs may or may not loadable using this loader (e.g. a classpath folder
     * can contain File or Jar URIs.
     */
    public Collection<URI> loadFolder(final URI folderUri, final String pattern)
    {
        try {
            for (final MigrationLoader loader : loaders) {
                if (loader.accept(folderUri)) {
                    return loader.loadFolder(folderUri, pattern);
                }
            }
            return null;
        }
        catch (IOException ioe) {
            throw new MigratoryException(Reason.INTERNAL, ioe);
        }
    }

    /**
     * Load a file from an URI.
     */
    public String loadFile(final URI fileUri)
    {
        try {
            for (final MigrationLoader loader : loaders) {
                if (loader.accept(fileUri)) {
                    return loader.loadFile(fileUri);
                }
            }
            return null;
        }
        catch (IOException ioe) {
            throw new MigratoryException(Reason.INTERNAL, ioe);
        }
    }
}
