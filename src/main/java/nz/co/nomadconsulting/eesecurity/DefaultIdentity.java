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
package nz.co.nomadconsulting.eesecurity;

import nz.co.nomadconsulting.eesecurity.authorisation.PermissionResolver;
import nz.co.nomadconsulting.eesecurity.idm.DefaultCredentials;
import nz.co.nomadconsulting.eesecurity.idm.IdentityManager;
import nz.co.nomadconsulting.eesecurity.idm.IdentityStoreUserEvent;
import nz.co.nomadconsulting.eesecurity.idm.LoggedIn;
import nz.co.nomadconsulting.eesecurity.idm.LoginFailedEvent;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;


@SuppressWarnings("serial")
@SessionScoped
@Named("identity")
public class DefaultIdentity implements Identity {

    @Inject
    private transient IdentityManager identityManager;

    @Inject
    private transient PermissionResolver permissionResolver;

    @Inject
    private DefaultCredentials credentials;

    @Inject
    private Event<LoginFailedEvent> loginFailedEvent;

    @Produces
    @Current
    private Object user;


    public void loggedInEventListener(@Observes @LoggedIn final IdentityStoreUserEvent event) {
        user = event.getUser();
    }


    @Override
    public void login() {
        final boolean success = identityManager.authenticate(
                credentials.getUsername(), credentials.getPassword());
        // TODO throw exception on login failure
        if (!success) {
            loginFailedEvent.fire(new LoginFailedEvent());
        }
    }


    @Override
    public void logout() {

    }


    @Override
    public boolean isLoggedIn() {
        return user != null;
    }


    @Override
    public boolean hasRole(final Object role) {
        return identityManager.hasRole(user, role, null);
    }


    @Override
    public boolean hasPermission(final Object resource, final String permission) {
        return permissionResolver.hasPermission(user, resource, permission);
    }


    public Object getUser() {
        return user;
    }
}
