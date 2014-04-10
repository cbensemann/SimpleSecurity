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
package nz.co.nomadconsulting.simplesecurity.authorisation;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;


public class PermissionResolver {

    @Inject
    private Instance<PermissionResolverConfiguration> configuration;

    private Set<PermissionStore> stores;


    @PostConstruct
    public void init() {
        final Set<PermissionStore> stores = configuration.get().getStores();
        this.stores = new HashSet<>(); // TODO deal with isUnsatisfied
        if (stores != null) {
            this.stores.addAll(stores);
        }
    }


    public boolean hasPermission(final Object identity, final Object resource, final String permission) {
        for (final PermissionStore store : stores) {
            final boolean hasPermission = store.hasPermission(identity,
                    resource, permission);
            if (hasPermission) {
                return true;
            }
        }
        return false;
    }
}
