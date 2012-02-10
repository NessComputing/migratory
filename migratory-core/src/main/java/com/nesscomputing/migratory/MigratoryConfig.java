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

public abstract class MigratoryConfig
{
    @Config("migratory.encoding")
    @Default("utf8")
    public String getEncoding()
    {
        return "utf8";
    }

    @Config("migratory.metadata_table")
    @Default("migratory_metadata")
    public String getMetadataTableName()
    {
        return "migratory_metadata";
    }

    @Config("migratory.readonly")
    @Default("false")
    public boolean isReadOnly()
    {
        return false;
    }

    @Config("migratory.create_personalities")
    @Default("true")
    public boolean isCreatePersonalities()
    {
        return true;
    }

    @Config("migratory.allow_rollforward")
    @Default("true")
    public boolean isAllowRollForward()
    {
        return true;
    }

    @Config("migratory.allow_rollback")
    @Default("false")
    public boolean isAllowRollBack()
    {
        return false;
    }

    @Config("migratory.http.login")
    @DefaultNull()
    public String getHttpLogin()
    {
        return null;
    }

    @Config("migratory.http.password")
    @DefaultNull()
    public String getHttpPassword()
    {
        return null;
    }
}
