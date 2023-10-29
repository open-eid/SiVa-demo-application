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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class SivaServiceTypeTest {

    @Test
    void testConstructorIsPrivate() throws Exception {
        final Constructor<SivaServiceType> constructor = SivaServiceType.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void givenJSONServiceNameReturnsTrue() {
        assertThat(SivaServiceType.JSON_SERVICE).isEqualTo("sivaJSON");
    }

    @Test
    void givenSOAPServiceNameReturnsTrue() {
        assertThat(SivaServiceType.SOAP_SERVICE).isEqualTo("sivaSOAP");
    }
}
