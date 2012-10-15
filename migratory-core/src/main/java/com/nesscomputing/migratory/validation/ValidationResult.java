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
package com.nesscomputing.migratory.validation;


import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import com.nesscomputing.migratory.metadata.MetadataInfo;

/**
 * Represents the result of a single validation.
 */
public class ValidationResult
{
    public enum ValidationStatus
    {
        OK, BAD,
        // Migration source unknown
        NOT_FOUND, BAD_CHECKSUM,
        // Migration type is not what was recorded
        TYPE_MISMATCH,
        // Last migration is in FAILED state
        FAILED_STATE;
    }

    private final ValidationStatus validationStatus;

    private final List<ValidationResultProblem> problems;

    ValidationResult(final ValidationStatus validationStatus, final List<ValidationResultProblem> problems)
    {
        this.validationStatus = validationStatus;
        this.problems = Collections.unmodifiableList(problems);
    }

    public ValidationStatus getValidationStatus()
    {
        return validationStatus;
    }

    public List<ValidationResultProblem> getProblems()
    {
        return problems;
    }

    public static class ValidationResultProblem
    {
        private final ValidationStatus validationStatus;
        private final String reason;
        private final MetadataInfo metadataInfo;

        private ValidationResultProblem(final ValidationStatus validationStatus, final MetadataInfo metadataInfo, final String reason)
        {
            this.validationStatus = validationStatus;
            this.metadataInfo = metadataInfo;
            this.reason = reason;
        }

        public ValidationStatus getValidationStatus()
        {
            return validationStatus;
        }

        public MetadataInfo getMetadataInfo()
        {
            return metadataInfo;
        }

        public String getReason()
        {
            return reason;
        }
    }

    public static final class Builder
    {
        private final List<ValidationResultProblem> problems = Lists.newArrayList();

        public Builder()
        {
        }

        public void add(final ValidationStatus validationStatus, MetadataInfo metadataInfo, final String reason)
        {
            problems.add(new ValidationResultProblem(validationStatus, metadataInfo, reason));
        }

        public ValidationResult build()
        {
            return new ValidationResult(problems.isEmpty() ? ValidationStatus.OK : ValidationStatus.BAD, problems);
        }
    }
}
