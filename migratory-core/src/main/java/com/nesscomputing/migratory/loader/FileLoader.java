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


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

/**
 * Loads arbitrary files from a file: URI.
 */
public class FileLoader implements MigrationLoader
{
    public static final Function<File, URI> FILE_TO_URL = new Function<File, URI>() {
        @Override
        public URI apply(@Nullable final File file) {
            return file == null ? null : file.toURI();
        }
    };

    private final Charset charset;

    public FileLoader(final Charset charset)
    {
        this.charset = charset;
    }

    @Override
    public boolean accept(final URI uri)
    {
        return (uri != null) && "file".equals(uri.getScheme());
    }

    @Override
    public Collection<URI> loadFolder(final URI folderUri, final String searchPattern)
    {
        final Pattern pattern = (searchPattern == null) ? null : Pattern.compile(searchPattern);

        final File folderLocation = new File(folderUri);
        if (folderLocation.exists() && folderLocation.canRead()) {
            if (folderLocation.isDirectory()) {
                final File [] files = (pattern == null) ? folderLocation.listFiles() : folderLocation.listFiles(new PatternFilenameFilter(pattern));
                return Collections2.transform(Arrays.asList(files), FILE_TO_URL);
            }
            else {
                throw new MigratoryException(Reason.INTERNAL, "%s is not a directory!", folderUri);
            }
        }
        else {
                throw new MigratoryException(Reason.INTERNAL, "Can not access %s!", folderUri);
        }
    }

    /**
     * This method loads a file from a file:/... URI. This is probably not what you are looking for.
     */
    @Override
    public String loadFile(final URI fileUri) throws IOException
    {
        final File fileLocation = new File(fileUri);
        if (fileLocation.exists() && fileLocation.canRead()) {
            if (fileLocation.isFile()) {
                return Files.toString(fileLocation, charset);
            }
            else {
                throw new MigratoryException(Reason.INTERNAL, "%s is not a file!", fileLocation);
            }
        }
        else {
            throw new MigratoryException(Reason.INTERNAL, "Can not access %s!", fileLocation);
        }
    }
}
