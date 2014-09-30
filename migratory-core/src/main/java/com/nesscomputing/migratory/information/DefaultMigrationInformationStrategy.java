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
package com.nesscomputing.migratory.information;


import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;


/**
 * Default strategy, understands URIs that are patterned like this:
 *
 * <personality-name>.<version>.{<mode>].<type>
 *
 * Where version could be either xxx which implies migrating from xxx-1 to xxx. or xxx-yyy which implies migration from xxx to yyy.
 *
 * If a mode is given as 'root', then the statements are executed through a special ("root") connection to the schema.
 * type is either 'sql' for plain sql or 'st' for stringtemplate.
 */
public class DefaultMigrationInformationStrategy implements MigrationInformationStrategy
{
    public MigrationInformation getInformation(final URI location)
    {
        if (location == null) {
            return null;
        }
        // Must be loaded from an URL, e.g. jar:file:... ends up on schema specific part and path is null.
        try {
            final String path = location.toURL().getPath();
            final String fileName = path.substring(path.lastIndexOf('/') + 1);
            final String [] pieces = StringUtils.split(fileName, '.');
            if (pieces.length < 3 || pieces.length > 4) {
                throw new MigratoryException(Reason.INTERNAL, "'%s' is not a valid migration name!", fileName);
            }

            final String [] versionPieces = StringUtils.split(pieces[1], '-');

            int startVersion = -1;
            int endVersion = -1;
            switch (versionPieces.length) {
                case 1:
                    endVersion = Integer.parseInt(versionPieces[0], 10);
                    if (endVersion < 1) {
                        throw new MigratoryException(Reason.INTERNAL, "'%s' has an end version of 0!", fileName);
                    }

                    startVersion = endVersion - 1;
                    break;

                case 2:
                    startVersion = Integer.parseInt(versionPieces[0], 10);
                    endVersion = Integer.parseInt(versionPieces[1], 10);

                    if (endVersion < 1) {
                        throw new MigratoryException(Reason.INTERNAL, "'%s' has an end version of 0!", fileName);
                    }

                    if (startVersion == endVersion) {
                        throw new MigratoryException(Reason.INTERNAL, "'%s' has the same start and end version!", fileName);
                    }
                    break;
                default:
                    throw new MigratoryException(Reason.INTERNAL, "Can not interpret '%s'!", fileName);
            }

            final boolean needsRoot = pieces.length == 4 && "root".equals(pieces[2].toLowerCase(Locale.ENGLISH));

            boolean template = false;

            if ("st".equals(pieces[pieces.length-1])) {
                template = true;
            }
            else if (!"sql".equals(pieces[pieces.length-1])) {
                throw new MigratoryException(Reason.INTERNAL, "'%s' has a bad suffix (not .st or .sql)!", fileName);
            }

            return new MigrationInformation(pieces[0], startVersion, endVersion, needsRoot, template);
        }
        catch (MalformedURLException mue) {
            throw new MigratoryException(Reason.INTERNAL, mue);
        }
    }
}
