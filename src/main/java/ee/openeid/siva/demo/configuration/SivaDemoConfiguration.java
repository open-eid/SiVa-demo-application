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

package ee.openeid.siva.demo.configuration;

import ee.openeid.siva.demo.ci.info.BuildInfo;
import ee.openeid.siva.demo.ci.info.FilesystemBuildInfoFileLoader;
import ee.openeid.siva.demo.monitoring.util.ManifestReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({
        SivaServiceProperties.class,
        BuildInfoProperties.class
})
@RequiredArgsConstructor
public class SivaDemoConfiguration {
    private final BuildInfoProperties properties;
    private final SivaServiceProperties proxyProperties;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return getBaseSslRestTemplateBuilder(restTemplateBuilder)
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }
    
    @Bean
    public ManifestReader manifestReader() {
        return new ManifestReader();
    }

    @SneakyThrows
    private RestTemplateBuilder getBaseSslRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder) {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(
                        new ClassPathResource(proxyProperties.getTrustStore()).getURL(),
                        proxyProperties.getTrustStorePassword().toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(10000, TimeUnit.MILLISECONDS)
                .setSocketTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .setDefaultConnectionConfig(connectionConfig)
                .build();
        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return restTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @Bean
    public BuildInfo displayBuildInfo() throws IOException {
        final FilesystemBuildInfoFileLoader buildInfoFileLoader = new FilesystemBuildInfoFileLoader();
        buildInfoFileLoader.setProperties(properties);
        return buildInfoFileLoader.loadBuildInfo();
    }
}
