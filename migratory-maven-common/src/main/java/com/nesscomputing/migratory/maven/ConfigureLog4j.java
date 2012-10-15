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
package com.nesscomputing.migratory.maven;

import java.net.URL;

import com.google.common.base.Preconditions;
import com.pyx4j.log4j.MavenLogAppender;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.maven.plugin.AbstractMojo;

public final class ConfigureLog4j
{
    private ConfigureLog4j()
    {
    }

    public static void start(final AbstractMojo mojo)
    {
        LogManager.resetConfiguration();

        final URL configFile = ConfigureLog4j.class.getResource("/log4j-maven.xml");
        Preconditions.checkNotNull(configFile, "no log4j-maven.xml file found!");

        DOMConfigurator.configure(configFile);
        MavenLogAppender.startPluginLog(mojo);

    }

    public static void stop(final AbstractMojo mojo)
    {
        MavenLogAppender.endPluginLog(mojo);
    }
}

