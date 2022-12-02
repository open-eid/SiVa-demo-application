/*
 * Copyright 2017 Riigi Infosüsteemide Amet
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

import ac.simons.spring.boot.wro4j.Wro4jAutoConfiguration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.domingosuarez.boot.autoconfigure.jade4j.Jade4JAutoConfiguration;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ee.openeid.siva.demo.cache.UploadFileCacheService;
import ee.openeid.siva.demo.cache.UploadedFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UploadController.class)
@ImportAutoConfiguration({Wro4jAutoConfiguration.class, Jade4JAutoConfiguration.class})
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebClient webClient;

    @MockBean
    private ValidationTaskRunner taskRunner;

    @MockBean
    private DataFilesTaskRunner dataFilesTaskRunner;

    @MockBean
    private HashcodeValidationTaskRunner hashcodeValidationTaskRunner;

    @MockBean
    private UploadFileCacheService hazelcastUploadFileCacheService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Before
    public void setUp() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @After
    public void tearDown() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
        reset(taskRunner);
        reset(hashcodeValidationTaskRunner);
        reset(dataFilesTaskRunner);
        Thread.interrupted();
    }

    @Test
    public void displayStartPageCheckPresenceOfUploadForm() throws Exception {
        final HtmlPage startPage = webClient.getPage("/");
        assertThat(startPage.getHtmlElementById("siva-dropzone").getAttribute("action")).isEqualTo("upload");
    }

    @Test
    public void uploadPageWithFileReturnsValidationResult() throws Exception {
        given(taskRunner.getValidationResult(any(ResultType.class)))
                .willReturn("{\"filename\": \"random.bdoc\", \"validSignaturesCount\": 1, \"signaturesCount\": 1}");

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename("random.bdoc");
        given(hazelcastUploadFileCacheService.addUploadedFile(anyLong(), any(MultipartFile.class), anyString()))
                .willReturn(uploadedFile);

        final MockMultipartFile uploadFile = new MockMultipartFile(
                "file",
                "random.bdoc",
                "application/vnd.etsi.asic-e+zip",
                "bdoc content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(uploadFile)
                .param("policy", "")
                .param("encodedFilename", "ranodom.bdoc")
                .param("returnDataFiles", "false")
                .param("type", "")
                .param("report", ""))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void fileUploadFailedRedirectedBackToStartPage() throws Exception {
        given(hazelcastUploadFileCacheService.addUploadedFile(anyLong(), any(MultipartFile.class), anyString()))
                .willThrow(new IOException("File upload failed"));

        final MockMultipartFile uploadFile = new MockMultipartFile(
                "file",
                "random.bdoc",
                "application/vnd.etsi.asic-e+zip",
                "bdoc content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(uploadFile)
                .param("policy", "")
                .param("encodedFilename", "ranodom.bdoc")
                .param("returnDataFiles", "false")
                .param("type", "")
                .param("report", ""))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    public void emptyFileUploadedRedirectsBackToStartPage() throws Exception {
        MockMultipartFile uploadFile = new MockMultipartFile(
                "file",
                "random.bdoc",
                "application/vnd.etsi.asic-e+zip",
                "".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(uploadFile)
                .param("policy", "")
                .param("encodedFilename", "ranodom.bdoc")
                .param("returnDataFiles", "false")
                .param("type", "")
                .param("report", ""))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    public void webServiceTaskRunnerThrowsInterruptedExceptionExpectLogMessage() throws Exception {
        doThrow(new InterruptedException("SiVa Service failure")).when(taskRunner).run(any(), any(), any());
        MockMultipartFile uploadFile = new MockMultipartFile(
                "file",
                "random.bdoc",
                "application/vnd.etsi.asic-e+zip",
                "bdoc content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(uploadFile)
                .param("policy", "")
                .param("encodedFilename", "ranodom.bdoc")
                .param("returnDataFiles", "false")
                .param("type", "")
                .param("report", ""))
                .andExpect(MockMvcResultMatchers.status().is(200));

        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getMessage()).contains("SiVa SOAP or REST service call failed with error:");

    }
}
