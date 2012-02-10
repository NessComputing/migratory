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
package com.nesscomputing.migratory;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public abstract class MigratoryDBIConfig
{
    @Config("${_migratory}driver")
    @DefaultNull
    public String getDBDriverClass()
    {
        return null;
    }

    @Config("${_migratory}url")
    @DefaultNull
    public String getDBUrl()
    {
        return null;
    }

    @Config("${_migratory}user")
    @DefaultNull
    public String getDBUser()
    {
        return null;
    }

    @Config("${_migratory}password")
    @DefaultNull
    public String getDBPassword()
    {
        return null;
    }

    @Config("${_migratory}reveal_password")
    @Default("false")
    public Boolean isRevealPassword()
    {
        return false;
    }
}
