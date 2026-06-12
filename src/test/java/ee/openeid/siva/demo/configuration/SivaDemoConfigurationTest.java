/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

package ee.openeid.siva.demo.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.UrlResource;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SivaDemoConfigurationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void restTemplateBeanInitializesWithDefaultClasspathTruststore() {
        assertNotNull(restTemplate);
    }

    @Test
    void restTemplateBeanInitializesWithFilePrefixTruststore(@TempDir Path tempDir) throws Exception {
        Path truststorePath = tempDir.resolve("truststore.p12");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("siva_server_truststore.p12")) {
            assertNotNull(is, "siva_server_truststore.p12 not found on test classpath");
            Files.copy(is, truststorePath);
        }

        SivaServiceProperties props = new SivaServiceProperties();
        props.setTrustStore(new UrlResource("file:" + truststorePath.toAbsolutePath()));
        props.setTrustStorePassword("password");

        RestTemplate result = new SivaDemoConfiguration(props).restTemplate(new RestTemplateBuilder());
        assertNotNull(result);
    }
}
