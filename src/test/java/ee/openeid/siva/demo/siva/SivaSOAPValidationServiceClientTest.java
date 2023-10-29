/*
 * Copyright 2017 - 2023 Riigi Infosüsteemi Amet
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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ee.openeid.siva.demo.cache.UploadedFile;
import ee.openeid.siva.demo.test.utils.TestFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


import static ee.openeid.siva.demo.siva.SivaSOAPValidationServiceClient.LINE_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SivaSOAPValidationServiceClientTest {
    @Autowired
    @Qualifier(value = "sivaSOAP")
    private ValidationService validationService;
    @MockBean
    private RestTemplate restTemplate;
    @Captor
    private ArgumentCaptor<String> validationRequestCaptor;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @BeforeEach
    void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    void tearDown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void givenValidRequestWillReturnSOAPValidationReport(@TempDir File testingFolder) throws Exception {
        String response = FileUtils.readFileToString(TestFileUtils.loadTestFile("/soap_response.xml"), StandardCharsets.UTF_8);
        serverMockResponse(response);
        UploadedFile uploadedFile = TestFileUtils.generateUploadFile(testingFolder, "hello.bdoc", "Valid document");

        String validatedDocument = validationService.validateDocument("", "", uploadedFile);

        Diff xmlDiff = DiffBuilder.compare(response).withTest(validatedDocument).ignoreWhitespace().build();
        assertThat(xmlDiff.hasDifferences()).isFalse();

        verify(restTemplate).postForObject(Mockito.anyString(), validationRequestCaptor.capture(), Mockito.any());
        assertThat(validationRequestCaptor.getValue()).contains("<Filename>hello.bdoc</Filename>");

    }

    @Test
    void givenValidRequestReturnsInvalidXMLReturnsEmptyString(@TempDir File testingFolder) throws Exception {
        serverMockResponse(StringUtils.EMPTY);
        UploadedFile uploadedFile = TestFileUtils.generateUploadFile(testingFolder, "hello.bdoc", "Valid document");
        String validatedDocument = validationService.validateDocument("", "", uploadedFile);

        assertThat(validatedDocument).isEqualTo(StringUtils.EMPTY);
        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getMessage()).contains("XML Parsing error:");
    }

    @Test
    void givenNullUploadFileWillThrowException() {
        IOException caughtException = assertThrows(
                IOException.class, () -> validationService.validateDocument(null, null, null)
        );
        assertEquals("File not found", caughtException.getMessage());

    }

    @Test
    void validXmlSoapCreation() {
        String request = SivaSOAPValidationServiceClient.createXMLValidationRequest("dGVzdA==", "filename.asice", "Simple", "POLv3");
        String expectedRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://soap.webapp.siva.openeid.ee/\">" + LINE_SEPARATOR +
                "   <soapenv:Header/>" + LINE_SEPARATOR +
                "   <soapenv:Body>" + LINE_SEPARATOR +
                "      <soap:ValidateDocument>" + LINE_SEPARATOR +
                "         <soap:ValidationRequest>" + LINE_SEPARATOR +
                "            <Document>dGVzdA==</Document>" + LINE_SEPARATOR +
                "            <Filename>filename.asice</Filename>" + LINE_SEPARATOR +
                "            <ReportType>Simple</ReportType>" + LINE_SEPARATOR +
                "            <SignaturePolicy>POLv3</SignaturePolicy>" + LINE_SEPARATOR +
                "         </soap:ValidationRequest>" + LINE_SEPARATOR +
                "      </soap:ValidateDocument>" + LINE_SEPARATOR +
                "   </soapenv:Body>" + LINE_SEPARATOR +
                "</soapenv:Envelope>";
        assertEquals(expectedRequest, request);
    }

    private void serverMockResponse(String response) {
        when(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(response);
    }
}
