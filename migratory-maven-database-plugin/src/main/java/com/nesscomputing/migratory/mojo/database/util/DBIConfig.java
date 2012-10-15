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
import org.skife.config.DefaultNull;

public interface DBIConfig
{
    @Config({"${_dbi_name}driver", "${_prefix}.default.driver"})
    @DefaultNull
    String getDBDriverClass();

    @Config({"${_dbi_name}url"})
    @DefaultNull
    String getDBUrl();

    @Config({"${_dbi_name}user", "${_prefix}.default.user"})
    @DefaultNull
    String getDBUser();

    @Config({"${_dbi_name}password", "${_prefix}.default.password"})
    @DefaultNull
    String getDBPassword();

    @Config({"${_dbi_name}tablespace", "${_prefix}.default.tablespace"})
    @DefaultNull
    String getDBTablespace();
}
