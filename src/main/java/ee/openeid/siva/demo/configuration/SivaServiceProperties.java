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

package ee.openeid.siva.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "siva.service")
public class SivaServiceProperties {
    private static final String DEFAULT_SERVICE_URL = "http://localhost:8080";
    private String servicePath = "/validate";
    private String hashcodeServicePath = "/validateHashcode";
    private String dataFilesServicePath = "/getDataFiles";
    private String serviceHost = DEFAULT_SERVICE_URL;
    private String trustStore = "siva_server_truststore.p12";
    @SuppressWarnings("squid:S2068") //default password
    private String trustStorePassword = "password";
}
