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
import ee.openeid.siva.demo.siva.DataFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DataFilesTaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFilesTaskRunner.class);
    private final AtomicReference<String> dataFilesResult = new AtomicReference<>();
    private DataFilesService dataFilesService;

    public void run(UploadedFile uploadedFile) throws InterruptedException {
        getDataFiles(uploadedFile);
    }

    private void getDataFiles(UploadedFile uploadedFile) {
        try {
            dataFilesResult.set(dataFilesService.getDataFiles(uploadedFile));
        } catch (IOException e) {
            LOGGER.warn("Uploaded file data files extraction failed with error: {}", e.getMessage(), e);
        }
    }

    public void clearDataFilesResults() {
        dataFilesResult.set(null);
    }

    public String getDataFilesResult() {
        return dataFilesResult.get();
    }

    @Autowired
    public void setDataFilesService(final DataFilesService dataFilesService) {
        this.dataFilesService = dataFilesService;
    }

}
