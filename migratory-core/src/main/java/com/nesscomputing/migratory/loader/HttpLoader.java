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
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.skife.config.CommonsConfigSource;
import org.skife.config.ConfigurationObjectFactory;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryConfig;
import com.nesscomputing.tinyhttp.HttpFetcher;
import com.nesscomputing.tinyhttp.ssl.SSLConfig;

/**
 * Loads arbitrary files from a http: or https: URI.
 */
public class HttpLoader implements MigrationLoader
{
    private static final Log LOG = Log.findLog();


    private final MigratoryConfig migratoryConfig;
    private final HttpFetcher httpFetcher;

    public HttpLoader(final MigratoryConfig migratoryConfig)
    {
        this.migratoryConfig = migratoryConfig;

        final ConfigurationObjectFactory objectFactory = new ConfigurationObjectFactory(new CommonsConfigSource(new SystemConfiguration().subset("config")));
        final SSLConfig sslConfig = objectFactory.build(SSLConfig.class);
        this.httpFetcher = new HttpFetcher(sslConfig);
    }

    @Override
    public boolean accept(final URI uri)
    {
        return (uri != null) && ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()));
    }

    @Override
    public String loadFile(final URI fileUri) throws IOException
    {
        LOG.trace("Trying to load '%s'...", fileUri);
        try {

            final String result = httpFetcher.get(fileUri, migratoryConfig.getHttpLogin(), migratoryConfig.getHttpPassword(), UTF8StringContentConverter.DEFAULT_CONVERTER);

            if (result != null) {
                LOG.trace("... succeeded");
                return result;
            }
            else {
                LOG.trace("... not found");
            }
        }
        catch (IOException ioe) {
            LOG.trace(ioe, "... failed");
        }
        return null;
    }


    @Override
    public Collection<URI> loadFolder(final URI folderUri, final String searchPattern) throws IOException
    {
        final List<URI> results = Lists.newArrayList();
        final Pattern pattern = (searchPattern == null) ? null : Pattern.compile(searchPattern);

        final String path = folderUri.getPath();
        final URI baseUri = (path.endsWith("/") ? folderUri : folderUri.resolve(path + "/"));

        final String content = loadFile(baseUri);
        if (content == null) {
            // File not found
            return results;
        }

        // The folders are a list of file names in plain text. Split it up.
        final String [] filenames = StringUtils.split(content);

        for (String filename : filenames) {
            if (pattern.matcher(filename).matches()) {
                results.add(URI.create(baseUri + filename));
            }
        }

        return results;
    }
}
