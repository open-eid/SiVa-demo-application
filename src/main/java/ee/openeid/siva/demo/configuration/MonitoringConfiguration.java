/*
 * Copyright 2016 - 2024 Riigi Infosüsteemi Amet
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

import ee.openeid.siva.demo.monitoring.indicator.UrlHealthIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public abstract class MonitoringConfiguration {

    public static final String DEFAULT_MONITORING_ENDPOINT = "/monitoring/health";
    public static final int DEFAULT_TIMEOUT = 10000;

    private final RestTemplate restTemplate;
    private final SivaServiceProperties proxyProperties;


    @Bean
    public UrlHealthIndicator link1() {
        UrlHealthIndicator.ExternalLink link = new UrlHealthIndicator.ExternalLink("sivaService",
                proxyProperties.getServiceHost() + MonitoringConfiguration.DEFAULT_MONITORING_ENDPOINT,
                MonitoringConfiguration.DEFAULT_TIMEOUT * 2);
        UrlHealthIndicator indicator = new UrlHealthIndicator();
        indicator.setExternalLink(link);
        indicator.setRestTemplate(restTemplate);
        return indicator;
    }

}
