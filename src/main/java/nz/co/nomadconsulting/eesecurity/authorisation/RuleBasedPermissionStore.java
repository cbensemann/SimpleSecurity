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
package nz.co.nomadconsulting.eesecurity.authorisation;

import javax.inject.Inject;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;


public class RuleBasedPermissionStore implements PermissionStore {

    @Inject
    private KieBase securityRules;


    @Override
    public boolean hasPermission(final Object identity, final Object resource, final String permission) {

        final KieSession session = securityRules.newKieSession();

        final PermissionCheck check = new PermissionCheck(resource, permission);

        session.insert(identity);
        session.insert(check);
        session.fireAllRules();

        return check.isGranted();
    }


    @Override
    public void grantPermission(final Object identity, final Object resource, final Object permission) {
        // TODO Auto-generated method stub

    }


    @Override
    public void revokePermission(final Object identity, final Object resource, final Object permission) {
        // TODO Auto-generated method stub

    }
}
