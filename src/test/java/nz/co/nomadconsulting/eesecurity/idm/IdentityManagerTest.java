/*
 * Copyright 2014 Nomad Consulting Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.nomadconsulting.eesecurity.idm;

import static org.assertj.core.api.Assertions.assertThat;

import nz.co.nomadconsulting.eesecurity.IdentityStoreConfiguration;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;


@RunWith(CdiRunner.class)
public class IdentityManagerTest {

    @Inject
    private IdentityManager identityManager;
    
    @Produces
    @Mock
    private IdentityStore store;
    
    @Test
    public void test() {
        assertThat(identityManager).isNotNull();
    }
    
    @Produces
    public IdentityStoreConfiguration configuration() {
        final IdentityStoreConfiguration config = new IdentityStoreConfiguration();
        config.setRoleClass(Object.class);
        config.setUserClass(Object.class);
        return config;
    }
}
