package com.nesscomputing.migratory.mojo.database.util;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

/**
 * The initial config is loaded directly from the System properties
 * (and the .migratory.properties file in the user's home). It
 * defines some settings that are required for the plugin to work.
 */
public abstract class InitialConfig
{
    /**
     * Default location of the manifest files and the sql files to load.
     */
    @Config("migratory.manifest.url")
    @DefaultNull
    public String getManifestUrl()
    {
        return null;
    }

    /**
     * Default manifest to load.
     */
    @Config("migratory.manifest.name")
    @Default("development")
    public String getManifestName()
    {
        return "development";
    }

    /**
     * Property prefix in the manifest files. Defaults to 'ness'.
     */
    @Config("migratory.default.property-prefix")
    @Default("ness")
    public String getDefaultPropertyPrefix()
    {
        return "ness";
    }
}
