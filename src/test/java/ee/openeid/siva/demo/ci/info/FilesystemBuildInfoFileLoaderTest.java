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

package ee.openeid.siva.demo.ci.info;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ee.openeid.siva.demo.configuration.BuildInfoProperties;
import ee.openeid.siva.demo.test.utils.TestFileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FilesystemBuildInfoFileLoaderTest {
    private FilesystemBuildInfoFileLoader loader = new FilesystemBuildInfoFileLoader();

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
    void givenValidInfoFileWillReturnCorrectBuildInfo() throws Exception {
        loader.setProperties(createBuildProperties("/test-info.yml"));
        BuildInfo buildInfo = loader.loadBuildInfo();

        assertThat(buildInfo.getTravisCi().getBuildNumber()).isEqualTo("#106");
        assertThat(buildInfo.getGithub().getAuthorName()).isEqualTo("Andres Voll");
        assertThat(buildInfo.getGithub().getShortHash()).isEqualTo("dfce8a94");
        assertThat(buildInfo.getGithub().getUrl()).contains("dfce8a94c54b8d94153807b153baaf56bfd9317e");
        assertThat(buildInfo.getTravisCi().getBuildUrl()).contains("135787867");
    }

    @Test
    void givenInvalidInfoFilePathWillReturnEmptyBuildInfo() throws Exception {
        BuildInfoProperties properties = new BuildInfoProperties();
        properties.setInfoFile("wrong.info-file.yml");

        loader.setProperties(properties);
        BuildInfo buildInfo = loader.loadBuildInfo();


        assertThat(buildInfo.getGithub()).isNull();
        assertThat(buildInfo.getTravisCi()).isNull();
    }

    @Test
    void givenInfoFileWithMissingGithubInfoWillReturnBuildInfo() throws Exception {
        loader.setProperties(createBuildProperties("/info-missing-github.yml"));
        BuildInfo buildInfo = loader.loadBuildInfo();

        assertThat(buildInfo.getGithub()).isNull();
        assertThat(buildInfo.getTravisCi().getBuildNumber()).isEqualTo("#106");
    }

    @Test
    void givenInvalidWindowsBuildInfoFilePathWillReturnEmptyBuildInfo() throws Exception {
        BuildInfoProperties properties = new BuildInfoProperties();
        properties.setInfoFile("C:\\invalid-build-info-path.yml");

        loader.setProperties(properties);
        BuildInfo buildInfo = loader.loadBuildInfo();

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        final List<LoggingEvent> loggingEvent = captorLoggingEvent.getAllValues();

        assertThat(loggingEvent.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.get(0).getFormattedMessage()).contains("No such file exists: C:\\invalid-build-info-path.yml");
        assertThat(buildInfo.getGithub()).isNull();
    }

    private static BuildInfoProperties createBuildProperties(String filePath) {
        File file = TestFileUtils.loadTestFile(filePath);

        BuildInfoProperties properties = new BuildInfoProperties();
        properties.setInfoFile(file.getAbsolutePath());
        return properties;
    }
}
