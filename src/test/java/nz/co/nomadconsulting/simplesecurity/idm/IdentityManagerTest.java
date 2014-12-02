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
package nz.co.nomadconsulting.simplesecurity.idm;

import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import nz.co.nomadconsulting.simplesecurity.IdentityStoreConfiguration;
import nz.co.nomadconsulting.simplesecurity.authorisation.RoleName;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.Id;

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
    public void testCreation() {
        assertThat(identityManager).isNotNull();
    }


    @Test
    public void testHasRole() {
        final TestUser user = new TestUser();
        final TestRole requestedRole = new TestRole("admin", null, null);

        assertThat(identityManager.hasRole(user, requestedRole)).isFalse();
        user.roles.add(requestedRole);
        assertThat(identityManager.hasRole(user, requestedRole)).isTrue();
    }


    @Test
    public void testHasRoleWithScope() {
        final TestUser user = new TestUser();
        final TestScope scope = new TestScope(123);
        final TestRole requestedRole = new TestRole("admin", scope.id.toString(), scope.getClass().getName());
        when(store.getIdentifier(scope)).thenReturn(scope.id);

        assertThat(identityManager.hasRole(user, requestedRole, scope)).isFalse();
        user.roles.add(requestedRole);
        assertThat(identityManager.hasRole(user, requestedRole, scope)).isTrue();
    }


    @Produces
    public IdentityStoreConfiguration configuration() {
        final IdentityStoreConfiguration config = new IdentityStoreConfiguration();
        config.setRoleClass(TestRole.class);
        config.setUserClass(TestUser.class);
        return config;
    }

    static class TestUser {
        @Roles
        private Set<TestRole> roles = new HashSet<TestRole>();


        public Set<TestRole> getRoles() {
            return roles;
        }


        public void setRoles(final Set<TestRole> roles) {
            this.roles = roles;
        }
    }

    static class TestRole {
        @RoleName
        private String name;

        @RoleScopeId
        private String scope;

        @RoleScopeClass
        private String scopeClass;


        public TestRole(final String name, final String scope, final String scopeClass) {
            this.name = name;
            this.scope = scope;
            this.scopeClass = scopeClass;
        }


        public String getName() {
            return name;
        }


        public void setName(final String name) {
            this.name = name;
        }


        public String getScope() {
            return scope;
        }


        public void setScope(final String scope) {
            this.scope = scope;
        }


        public String getScopeClass() {
            return scopeClass;
        }


        public void setScopeClass(final String scopeClass) {
            this.scopeClass = scopeClass;
        }
    }

    static class TestScope {
        @Id
        private Long id;


        public TestScope(long id) {
            this.id = id;
        }


        public Long getId() {
            return id;
        }


        public void setId(final Long id) {
            this.id = id;
        }
    }
}
