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
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.concurrent.Immutable;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.nesscomputing.tinyhttp.HttpContentConverter;

/**
 * Load an UTF-8 string from HTTP.
 */
@Immutable
public class UTF8StringContentConverter implements HttpContentConverter<String>
{
    public static final HttpContentConverter<String> DEFAULT_CONVERTER = new UTF8StringContentConverter();

    public UTF8StringContentConverter()
    {
    }

    @Override
    public String convert(HttpRequest httpClientRequest, HttpResponse httpClientResponse, InputStream inputStream) throws IOException
    {
        final int responseCode = httpClientResponse.getStatusLine().getStatusCode();
        switch (responseCode) {
            case 200:
            case 201:
                final InputStreamReader reader = new InputStreamReader(inputStream, Charsets.UTF_8);

                try {
                    return CharStreams.toString(reader);
                }
                finally {
                    Closeables.closeQuietly(reader);
                }

            case 204:
                return "";

            default:
                throw new IOException("Could not load file from " + httpClientRequest.getRequestLine().getUri() + " (" + httpClientResponse.getStatusLine().getStatusCode() + ")");
        }
    }


}
