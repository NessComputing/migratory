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
package com.nesscomputing.migratory.jdbi;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

public final class JdbiSupport
{
    private JdbiSupport()
    {
    }

    public static int execute(final Handle handle, final String query)
    {
        return handle.createStatement(query).execute();
    }

    public static int getInteger(final Handle handle, final String query)
    {
        return handle.createQuery(query)
        .map(IntegerMapper.FIRST)
        // Work around an *incredibly* obscure bug in hsqldb 1.8.0.x where setMaxRows(1)
        // (which first does) will only return 0 (no rows) or 1 (rows) for COUNT(*)
        .setMaxRows(0)
        .first();
    }
}
