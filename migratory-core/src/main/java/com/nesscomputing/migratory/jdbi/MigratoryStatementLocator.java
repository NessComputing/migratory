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

import java.io.StringReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class MigratoryStatementLocator implements StatementLocator
{
    public static final String SQL_LOCATION = "/migratory/sql/";

    private static final Logger LOG = LoggerFactory.getLogger(MigratoryStatementLocator.class);

    private final Map<String, String> sql = new ConcurrentHashMap<String, String>();

    public void addTemplate(final String identifier, final String rawSql)
    {
        sql.put(identifier, rawSql);
    }

    @Override
    public String locate(final String statementName, final StatementContext context) throws Exception
    {
        context.setAttribute(MigratoryStatementRewriter.SKIP_REWRITE, null);

        if (StringUtils.isEmpty(statementName)) {
            throw new IllegalStateException("Statement Name can not be empty/null!");
        }

        // This is a recorded statement that comes from some loader. This needs
        // to be preregistered using addTemplate, so look there.
        if (statementName.charAt(0) == '@') {
            LOG.trace("Retrieving statement: {}", statementName);
            final String rawSql = sql.get(statementName);

            if (rawSql == null) {
                throw new IllegalStateException("Statement '" + statementName + "' not registered!");
            }

            // @T is a template.
            if (statementName.charAt(1) == 'T') {
                return templatize(rawSql, context);
            }
            else {
                context.setAttribute(MigratoryStatementRewriter.SKIP_REWRITE, Boolean.TRUE);
                return rawSql;
            }
        }
        // Or is it one of the internal statements used by
        // migratory to do its housekeeping? If yes, load it from the
        // predefined location on the class path.
        else if (statementName.charAt(0) == '#') {
            // Multiple templates can be in a string template group. In that case, the name is #<template-group:<statement name>
            final String [] statementNames = StringUtils.split(statementName.substring(1), ":");

            final String sqlLocation = SQL_LOCATION + context.getAttribute("db_type") + "/" + statementNames[0] + ".st";

            LOG.trace("Loading SQL: {}", sqlLocation);
            final URL location = Resources.getResource(MigratoryStatementLocator.class, sqlLocation);
            if (location == null) {
                throw new IllegalArgumentException("Location '" + sqlLocation + "' does not exist!");
            }
            final String rawSql = Resources.toString(location, Charsets.UTF_8);

            if (statementNames.length == 1) {
                // Plain string template file. Just run it.
                return templatize(rawSql, context);
            }
            else {
                final StringTemplateGroup group = new StringTemplateGroup(new StringReader(rawSql), AngleBracketTemplateLexer.class);
                LOG.trace("Found {} in {}", group.getTemplateNames(), location);

                final StringTemplate template = group.getInstanceOf(statementNames[1]);
                template.setAttributes(context.getAttributes());
                final String sql = template.toString();

                LOG.trace("SQL: {}", sql);
                return sql;
            }
        }
        // Otherwise, it is raw SQL that was run on the database. Pass it through.
        else {
            context.setAttribute(MigratoryStatementRewriter.SKIP_REWRITE, Boolean.TRUE);
            return statementName;
        }
    }

    private String templatize(final String rawSql, final StatementContext context)
    {
        // This is not very effective but then again, most templates will be used only once and
        // this code is not intended to be run 1000 times a second anyway.

        final StringTemplate template = new StringTemplate(rawSql, AngleBracketTemplateLexer.class);
        template.setAttributes(context.getAttributes());
        final String sql = template.toString();

        LOG.trace("SQL: {}", sql);
        return sql;
    }
}
