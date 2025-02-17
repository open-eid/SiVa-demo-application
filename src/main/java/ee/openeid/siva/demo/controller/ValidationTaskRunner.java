/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package ee.openeid.siva.demo.controller;

import ee.openeid.siva.demo.cache.UploadedFile;
import ee.openeid.siva.demo.siva.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Service
class ValidationTaskRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTaskRunner.class);
    private final AtomicReference<String> validationResult = new AtomicReference<>();
    private ValidationService validationService;

    void run(String policy, String report, UploadedFile uploadedFile) throws InterruptedException {
        validateFile(report, uploadedFile, policy);
    }

    private void validateFile(
            String report,
            UploadedFile uploadedFile,
            String policy
    ) {
        try {
            validationResult.set(validationService.validateDocument(policy, report, uploadedFile));
        } catch (IOException e) {
            LOGGER.warn("Uploaded file validation failed with error: {}", e.getMessage(), e);
        }
    }

    public String getValidationResult() {
        return validationResult.get();
    }

    public void clearValidationResults() {
        validationResult.set(null);
    }

    @Autowired
    public void setValidationService(final ValidationService validationService) {
        this.validationService = validationService;
    }
}
