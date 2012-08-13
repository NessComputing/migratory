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
