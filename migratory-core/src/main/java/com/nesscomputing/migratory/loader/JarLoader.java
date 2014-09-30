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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

/**
 * Loads arbitrary files from a file: URI.
 */
public class JarLoader implements MigrationLoader
{
    private final Charset charset;

    public JarLoader(final Charset charset)
    {
        this.charset = charset;
    }

    @Override
    public boolean accept(final URI uri)
    {
        return uri != null && "jar".equals(uri.getScheme());
    }

    @Override
    public Collection<URI> loadFolder(final URI location, final String searchPattern) throws IOException
    {
        final Pattern pattern = (searchPattern == null) ? null : Pattern.compile(searchPattern);

        final String path = location.getSchemeSpecificPart();
        final int bangIndex = path.indexOf("!");
        if (bangIndex > 0) {
            final String jarPath = path.substring(5, bangIndex); // strip out jar:/, all the way to the bang.
            final String basePath = "jar:" + path.substring(0, bangIndex + 2);
            final String locationPath = path.substring(bangIndex + 2); // skip over !/
            final JarFile jar = new JarFile(jarPath); // URI.getPath is decoded.

            try {
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                final Set<URI> results = Sets.newHashSet();

                while(entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();

                    String entryName = entry.getName();
                    if (entryName.startsWith(locationPath)) { //filter according to the path
                        entryName = entryName.substring(locationPath.length() + 1);
                        int checkSubdir = entryName.indexOf("/");
                        // Skip Directories
                        if ((!entryName.isEmpty() && checkSubdir < 0) && (pattern == null || pattern.matcher(entryName).matches())) { // NOPMD
                            results.add(URI.create(basePath + entry.getName()));
                        }
                    }
                }
                return results;
            }
            finally {
                jar.close();
            }
        }
        else {
            throw new MigratoryException(Reason.INTERNAL, "Can not parse jar URI '%s!", location);
        }
    }

    /**
     * This method loads a file from a file:/... URI. This is probably not what you are looking for.
     */
    @Override
    public String loadFile(final URI fileUri) throws IOException
    {
        return Resources.toString(fileUri.toURL(), charset);
    }
}
