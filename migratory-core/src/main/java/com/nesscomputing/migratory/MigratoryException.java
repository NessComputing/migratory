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

public final class MigratoryException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public enum Reason
    {
        INIT,  INTERNAL, DATABASE, IS_READONLY, NEW_PERSONALITIES_DENIED, ROLL_FORWARD_DENIED, ROLL_BACK_DENIED, VALIDATION_FAILED;
    }

    private final Reason reason;

    public MigratoryException(final Reason reason)
    {
        super();
        this.reason = reason;
    }

    public MigratoryException(final Reason reason, final Throwable t)
    {
        super(t);
        this.reason = reason;
    }

    public MigratoryException(final Reason reason, final String message, final Object ... args)
    {
        super(String.format(message, args));
        this.reason = reason;
    }

    public MigratoryException(final Reason reason, final Throwable t, final String message, final Object ... args)
    {
        super(String.format(message, args), t);
        this.reason = reason;
    }

    public Reason getReason()
    {
        return reason;
    }
}

