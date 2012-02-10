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


import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * An immutable variant of the MigratoryDBI configuration object. Supports only URI, user and password.
 */
public final class ImmutableMigratoryDBIConfig extends MigratoryDBIConfig
{
    private final String dbUri;
    private final String user;
    private final String password;

    public ImmutableMigratoryDBIConfig(@Nonnull final String dbUri, final String user, final String password)
    {
        Preconditions.checkArgument(dbUri != null, "database uri can not be null!");
        this.dbUri = dbUri;
        this.user = user;
        this.password = password;
    }

    @Override
    public String getDBUrl()
    {
        return dbUri;
    }

    @Override
    public String getDBUser()
    {
        return user;
    }

    @Override
    public String getDBPassword()
    {
        return password;
    }
}
