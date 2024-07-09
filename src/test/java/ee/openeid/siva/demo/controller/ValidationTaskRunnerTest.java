/*
 * Copyright 2017 - 2024 Riigi Infosüsteemi Amet
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ee.openeid.siva.demo.cache.UploadedFile;
import ee.openeid.siva.demo.siva.ValidationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ValidationTaskRunnerTest {
    @Autowired
    private ValidationTaskRunner validationTaskRunner;

    @MockBean
    private ValidationService validationService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @BeforeEach
    void setUp() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);

        given(validationService.validateDocument(any(String.class), any(String.class), any(UploadedFile.class)))
                .willReturn("{}");
    }

    @AfterEach
    void tearDown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void givenValidUploadFileReturnsValidationResultOfAllServices() throws Exception {
        validationTaskRunner.run("", "", new UploadedFile());

        assertThat(validationTaskRunner.getValidationResult(ResultType.JSON)).isEqualTo("{}");
    }

    @Test
    void givenClearCommandReturnsEmptyMapValueAsNull() throws Exception {
        validationTaskRunner.run("", "", new UploadedFile());
        validationTaskRunner.clearValidationResults();

        assertThat(validationTaskRunner.getValidationResult(ResultType.JSON)).isNull();
    }

    @Test
    void validationServiceThrowsExceptionLogMessageIsWritten() throws Exception {
        given(validationService.validateDocument(any(String.class), any(String.class), any(UploadedFile.class))).willThrow(new IOException());
        validationTaskRunner.run("", "", new UploadedFile());

        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).contains("Uploaded file validation failed with error");
    }
}
