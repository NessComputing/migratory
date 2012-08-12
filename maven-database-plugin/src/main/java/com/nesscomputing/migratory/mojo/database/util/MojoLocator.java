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
