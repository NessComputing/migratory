package com.nesscomputing.migratory.maven;

import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.maven.plugin.AbstractMojo;

import com.google.common.base.Preconditions;
import com.pyx4j.log4j.MavenLogAppender;

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

