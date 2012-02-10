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
package com.nesscomputing.migratory.loader.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class StringConverter implements HttpContentConverter<String>
{
    private static final Logger LOG = LoggerFactory.getLogger(StringConverter.class);

    private final Charset charset;

    public StringConverter(final Charset charset)
    {
        this.charset = charset;
    }

    @Override
    public String convert(final HttpRequest request, final HttpResponse response, final InputStream inputStream)
    throws IOException
    {
        switch (response.getStatusLine().getStatusCode())
        {
            case HttpServletResponse.SC_NOT_FOUND:
                return null;
            case HttpServletResponse.SC_UNAUTHORIZED:
                LOG.warn("Could not load configuration from '" + request.getRequestLine().getUri() + "', not authorized!");
                return null;
            case HttpServletResponse.SC_OK:
                return CharStreams.toString(new InputStreamReader(inputStream, charset));
            default:
                throw new IOException("Could not load file from " + request.getRequestLine().getUri() + " (" + response.getStatusLine().getStatusCode() + ")");
        }
    }
}
