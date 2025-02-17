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

package ee.openeid.siva.demo.siva;

import ee.openeid.siva.demo.cache.UploadedFile;
import ee.openeid.siva.demo.test.utils.TestFileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SivaValidationServiceClientTest {

    @Autowired
    private ValidationService validationService;
    @Captor
    private ArgumentCaptor<ValidationRequest> validationRequestCaptor;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void validRequestReturnsCorrectValidationResult(@TempDir File testingFolder) throws Exception {
        final String mockResponse = mockServiceResponse();
        final String fileContents = "Hello Testing World";
        final String filename = "testing.bdoc";
        final UploadedFile inputFile = TestFileUtils.generateUploadFile(testingFolder, filename, fileContents);

        final String result = validationService.validateDocument("", "", inputFile);
        assertEquals(mockResponse, result);

        verify(restTemplate).postForObject(anyString(), validationRequestCaptor.capture(), any());

        assertEquals(filename, validationRequestCaptor.getValue().getFilename());
        assertEquals(Base64Utils.encodeToString(fileContents.getBytes()), validationRequestCaptor.getValue().getDocument());
    }

    @Test
    void invalidFileTypeGivenRequestDocumentTypeIsNull(@TempDir File testingFolder) throws Exception {
        mockServiceResponse();

        final UploadedFile file = TestFileUtils.generateUploadFile(testingFolder, "testing.exe", "error in file");
        validationService.validateDocument("POLv3","Simple",  file);

        verify(restTemplate).postForObject(anyString(), validationRequestCaptor.capture(), any());
        assertEquals(null, validationRequestCaptor.getValue().getDocumentType());
    }

    @Test
    void inputFileIsNullThrowsException() {
        IOException caughtException = assertThrows(
                IOException.class, () -> validationService.validateDocument(null, null, null)
        );
        assertEquals("Invalid file object given", caughtException.getMessage());
    }

    @Test
    void givenRestServiceIsUnreachableReturnsGenericSystemError(@TempDir File testingFolder) throws Exception {
        final UploadedFile file = TestFileUtils.generateUploadFile(testingFolder, "testing.bdoc", "simple file");
        BDDMockito.given(restTemplate.postForObject(anyString(), any(ValidationRequest.class), any()))
                .willThrow(new ResourceAccessException("Failed to connect to SiVa REST"));

        String result = validationService.validateDocument("", "", file);
        verify(restTemplate).postForObject(anyString(), validationRequestCaptor.capture(), any());

        MatcherAssert.assertThat(result, Matchers.containsString("errorCode"));
        MatcherAssert.assertThat(result, Matchers.containsString("errorMessage"));
    }

    private String mockServiceResponse() {
        final String mockResponse = "{\"jsonValidationResult\": \"TOTAL-PASSED\"}";
        BDDMockito.given(restTemplate.postForObject(anyString(), any(ValidationRequest.class), any()))
                .willReturn(mockResponse);

        return mockResponse;
    }
}
