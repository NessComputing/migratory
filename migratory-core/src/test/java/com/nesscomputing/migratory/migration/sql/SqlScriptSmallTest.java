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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.migratory.migration.sql.SqlScript;
import com.nesscomputing.migratory.migration.sql.SqlStatement;

/**
 * Test for SqlScript.
 */
public class SqlScriptSmallTest
{

    private SqlScript sqlScript = null;
    private List<String> lines = null;

    @Before
    public void setUp()
    {
        lines = new ArrayList<String>();
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(sqlScript);
        Assert.assertNotNull(lines);
        sqlScript = null;
        lines = null;
    }

    @Test
    public void stripSqlCommentsNoComment()
    {
        lines.add("select * from table;");
        sqlScript = new SqlScript(StringUtils.join(lines, "\n"));
        final Collection<SqlStatement> statements = sqlScript.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(1, statements.size());
        final Iterator<SqlStatement> it =  statements.iterator();
        Assert.assertEquals("select * from table", it.next().getSql());
    }

    @Test
    public void stripSqlCommentsSingleLineComment() {
        lines.add("--select * from table;");
        sqlScript = new SqlScript(StringUtils.join(lines, "\n"));
        final Collection<SqlStatement> statements = sqlScript.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(0, statements.size());
    }

    @Test
    public void stripSqlCommentsMultiLineCommentSingleLine() {
        lines.add("/*comment line*/");
        sqlScript = new SqlScript(StringUtils.join(lines, "\n"));
        final Collection<SqlStatement> statements = sqlScript.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(0, statements.size());
    }

    @Test
    public void stripSqlCommentsMultiLineCommentMultipleLines() {
        lines.add("/*comment line");
        lines.add("more comment text*/");
        sqlScript = new SqlScript(StringUtils.join(lines, "\n"));
        final Collection<SqlStatement> statements = sqlScript.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(0, statements.size());
    }

    @Test
    public void linesToStatements() {
        lines.add("select col1, col2");
        lines.add("from mytable");
        lines.add("where col1 > 10;");
        sqlScript = new SqlScript(StringUtils.join(lines, "\n"));
        final Collection<SqlStatement> statements = sqlScript.getSqlStatements();
        Assert.assertNotNull(statements);
        Assert.assertEquals(1, statements.size());
        final Iterator<SqlStatement> it =  statements.iterator();
        Assert.assertEquals("select col1, col2 from mytable where col1 > 10", it.next().getSql());
    }
}
