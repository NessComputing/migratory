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
package com.nesscomputing.migratory.locator;


import java.util.Map;

import com.nesscomputing.migratory.migration.Migration;


public interface MigrationLocator
{
    /**
     * Contribute all known migrations for a requested personality and database type.
     */
    void contributeMigrations(Map<String, Migration> migrations, String personalityName, String databaseType);

    /**
     * Returns true if this loader should be queried if internal migrations (e.g. loading the metadata table information)
     * are done. External loader implementations should return false.
     */
    boolean isSystemLoader();
}
