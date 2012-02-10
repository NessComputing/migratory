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
package com.nesscomputing.migratory.dbsupport.postgresql;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nesscomputing.migratory.migration.sql.SqlScript;


/**
 * SqlScript supporting PostgreSQL routine definitions.
 */
public class PostgreSQLSqlScript extends SqlScript
{
    public static final Pattern POSTGRES_FUNCTION_AS = Pattern.compile(".*AS\\s+(\\$\\w*\\$|')(.*)", Pattern.CASE_INSENSITIVE);

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     */
    public PostgreSQLSqlScript(final String rawSql)
    {
        super(rawSql);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter)
    {
        String upperCaseStatement = statement.toUpperCase();

        if (upperCaseStatement.startsWith("CREATE") && upperCaseStatement.contains("FUNCTION")) {
            // We have met a function definition. Let's see whether we found an 'AS'.
            Matcher m = POSTGRES_FUNCTION_AS.matcher(upperCaseStatement);
            if (m.matches()) {
                // The current delimiter is now not in use. Keep accumulating elements until we match it again.
                final String functionDelimiter = m.group(1);
                String remainder = m.group(2);

                if (functionDelimiter.length() > 1) {
                    // Something like $$ or $ABC$. Those can't be escaped.
                    if (remainder.contains(functionDelimiter)) {
                        // We have the closing match found. Return the default delimiter
                        return DEFAULT_STATEMENT_DELIMITER;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    while (remainder.length() > 0) {
                        int possibleMatch = remainder.indexOf(functionDelimiter);
                        if (possibleMatch == -1) {
                            return null;
                        }
                        remainder = remainder.substring(possibleMatch + 1);
                        if (!remainder.startsWith(functionDelimiter)) {
                            // Found a closing match and it is not followed
                            // directly by another. So it is not escaped. Return
                            return DEFAULT_STATEMENT_DELIMITER;
                        }
                        // Skip the match
                        remainder = remainder.substring(1);
                    }
                    return null;
                }
            }
        }
        return delimiter;
    }
}
