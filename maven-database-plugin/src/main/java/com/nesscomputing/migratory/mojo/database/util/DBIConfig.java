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
