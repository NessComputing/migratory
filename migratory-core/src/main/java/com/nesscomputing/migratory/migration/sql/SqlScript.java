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
package com.nesscomputing.migratory.migration.sql;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import com.nesscomputing.logging.Log;
import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

/**
 * Sql script containing a series of statements terminated by semi-columns (;). Single-line (--) and multi-line (/* * /)
 * comments are stripped and ignored.
 */
public class SqlScript
{
    private static final Log LOG = Log.findLog();

    /**
     * The default Statement delimiter.
     */
    protected static final String DEFAULT_STATEMENT_DELIMITER = ";";

    /**
     * The sql statements contained in this script.
     */
    private final Collection<SqlStatement> sqlStatements;

    public SqlScript(final String rawSql)
    {
        this.sqlStatements = parse(rawSql);
    }

    public SqlScript(final Collection<SqlStatement> sqlStatements)
    {
        this.sqlStatements = sqlStatements;
    }

    /**
     * @return The sql statements contained in this script.
     */
    public Collection<SqlStatement> getSqlStatements()
    {
        return sqlStatements;
    }

    private List<SqlStatement> parse(final String rawSql)
    {
        try {
            Reader reader = new StringReader(rawSql);
            final List<String> lines = readLines(reader);
            return linesToStatements(lines);
        }
        catch (IOException ioe) {
            throw new MigratoryException(Reason.INTERNAL, ioe, "Could not read String '%s'", rawSql);
        }
    }

    List<String> readLines(final Reader reader) throws IOException
    {
        final List<String> lines = Lists.newArrayList();

        final BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        boolean inMultilineComment = false;
        while ((line = StringUtils.trim(bufferedReader.readLine())) != null)
        {
            if (!inMultilineComment) {
                if (isCommentDirective(line) || line.startsWith("--")) {
                    LOG.trace("Ignored '%s'", line);
                    continue;
                }
                else if (line.contains("--")) {
                    lines.add(StringUtils.trim(line.substring(0, line.indexOf("--"))));
                }
                else if (line.startsWith("/*")) {
                    inMultilineComment = true;
                    LOG.trace("Start Multiline ignore at  '%s'", line);
                    if (line.endsWith("*/")) {
                    	LOG.trace("...and then immediately ending it");
                    	inMultilineComment = false;
                    }
                    continue;
                }
                else {
                    LOG.trace("Adding '%s' to output", line);
                    lines.add(line);
                }
            }
            else {
                if (line.endsWith("*/")) {
                    LOG.trace("Ending Multiline ignore at  '%s'", line);
                    inMultilineComment = false;
                }
            }
        }

        return lines;
    }

    /**
     * Turns these lines in a series of statements.
     */
    List<SqlStatement> linesToStatements(List<String> lines)
    {
        final List<SqlStatement> statements = Lists.newArrayList();
        final StringBuilder statementSql = new StringBuilder();
        int count = 0;

        String delimiter = DEFAULT_STATEMENT_DELIMITER;

        for (final String line : lines)
        {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            if (statementSql.length() > 0) {
                statementSql.append(" ");
            }
            statementSql.append(line);

            final String oldDelimiter = delimiter;
            delimiter = changeDelimiterIfNecessary(statementSql.toString(), line, delimiter);
            if (!StringUtils.equals(delimiter, oldDelimiter) && isDelimiterChangeExplicit()) {
                statementSql.setLength(0);
                continue; // for
            }

            if (StringUtils.endsWith(line, delimiter)) {
                // Trim off the delimiter at the end.
                statementSql.setLength(statementSql.length() - delimiter.length());
                statements.add(new SqlStatement(count++, StringUtils.trimToEmpty(statementSql.toString())));
                LOG.debug("Found statement: %s", statementSql);

                if (!isDelimiterChangeExplicit()) {
                    delimiter = DEFAULT_STATEMENT_DELIMITER;
                }
                statementSql.setLength(0);
            }
        }

        // Catch any statements not followed by delimiter.
        if (statementSql.length() > 0) {
            statements.add(new SqlStatement(count++, StringUtils.trimToEmpty(statementSql.toString())));
        }

        return statements;
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     */
    protected String changeDelimiterIfNecessary(final String statement, final String line, final String delimiter)
    {
        return delimiter;
    }

    /**
     * @return {@code true} if this database uses an explicit delimiter change statement. {@code false} if a delimiter
     *         change is implied by certain statements.
     */
    protected boolean isDelimiterChangeExplicit()
    {
        return false;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     */
    protected boolean isCommentDirective(String line) {
        return false;
    }
}
