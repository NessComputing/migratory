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
package com.nesscomputing.migratory.maven;


import java.util.List;
import java.util.Map;

import com.nesscomputing.migratory.Migratory;
import com.nesscomputing.migratory.maven.util.FormatInfo;
import com.nesscomputing.migratory.validation.ValidationResult;
import com.nesscomputing.migratory.validation.ValidationResult.ValidationResultProblem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Maven goal to validate the applied migrations in the database against the available classpath migrations in order to
 * detect accidental migration changes.
 *
 * @goal validate
 */
public class ValidateMojo extends AbstractMigratoryMojo
{
    private static final Logger CONSOLE = LoggerFactory.getLogger("console");

    private static final FormatInfo SHORT = new FormatInfo(
        "+---------------------+--------------------+--------------------------------+",
        "| %-40s | %-30s |\n",
        "| Problem             | Reason                                              |",
        "| %-20s | %53s |\n"
    );

    /**
     * @parameter expression="${personalities}"
     */
    @SuppressFBWarnings("UWF_NULL_FIELD")
    protected String personalities = null;

    @Override
    protected void doExecute(final Migratory migratory) throws Exception
    {
        final List<String> personalityList = parsePersonalities(personalities);

        final Map<String, ValidationResult> validationResults = migratory.dbValidate(personalityList, optionList);
        for (Map.Entry<String, ValidationResult> validationResult : validationResults.entrySet()) {
            dump(validationResult.getKey(), validationResult.getValue());
        }
    }

    public static final void dump(final String personality, final ValidationResult result)
    {
        if (result == null) {
            return;
        }

        final FormatInfo formatInfo = SHORT;

        CONSOLE.info("{}", formatInfo.getFrame());
        CONSOLE.info("{}", String.format(formatInfo.getName(), personality, result.getValidationStatus()));
        CONSOLE.info("{}", formatInfo.getFrame());
        final List<ValidationResultProblem> problems = result.getProblems();
        if (!problems.isEmpty()) {
            CONSOLE.info("{}", formatInfo.getHeader());
            CONSOLE.info("{}", formatInfo.getFrame());
            for (ValidationResultProblem problem: problems) {
                CONSOLE.info("{}", String.format(formatInfo.getFormat(),
                         problem.getValidationStatus(),
                         problem.getReason()));
                CONSOLE.info("{}", formatInfo.getFrame());
            }
        }
    }
}
