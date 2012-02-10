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


import org.skife.jdbi.v2.exceptions.CallbackFailedException;

import com.nesscomputing.migratory.MigratoryException.Reason;

class AbstractMigratorySupport
{
    protected MigratoryException processException(final Exception e)
    {
        Throwable toProcess = e;
        if (toProcess instanceof CallbackFailedException) {
            toProcess = e.getCause();
        }

        if (toProcess instanceof MigratoryException) {
            return (MigratoryException) toProcess;
        }
        else {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            return new MigratoryException(Reason.INTERNAL, toProcess);
        }
    }
}
