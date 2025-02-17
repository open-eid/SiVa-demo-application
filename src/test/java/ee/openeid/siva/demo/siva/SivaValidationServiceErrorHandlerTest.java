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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SivaValidationServiceErrorHandlerTest {
    private static final byte[] EMPTY_BODY = new byte[0];
    private ResponseErrorHandler errorHandler = new SivaValidationServiceErrorHandler();

    @Mock
    private ClientHttpResponse httpResponse;

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
    void givenNoneErrorCodeReturnsFalse() throws Exception {
        createResponse(HttpStatus.OK);
        assertFalse(errorHandler.hasError(httpResponse));
    }

    @Test
    void givenClientErrorCodeReturnsTrue() throws Exception {
        createResponse(HttpStatus.BAD_REQUEST);
        assertTrue(errorHandler.hasError(httpResponse));
    }

    @Test
    void givenServerErrorReturnsTrue() throws Exception {
        createResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(errorHandler.hasError(httpResponse));
    }

    @Test
    void givenUserErrorStatusCodeWillLogWarnErrorMessage() throws Exception {
        errorHandler.handleError(new MockClientHttpResponse(EMPTY_BODY, HttpStatus.BAD_REQUEST));
        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertEquals(loggingEvent.getLevel(), Level.ERROR);
        MatcherAssert.assertThat(loggingEvent.getFormattedMessage(), Matchers.containsString("400 BAD_REQUEST Bad Request"));
    }

    private ClientHttpResponse createResponse(HttpStatus status) throws IOException {
        when(httpResponse.getStatusCode()).thenReturn(status);
        return httpResponse;
    }
}
