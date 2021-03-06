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

import nz.co.nomadconsulting.simplesecurity.IdentityStoreConfiguration;
import nz.co.nomadconsulting.simplesecurity.authorisation.RoleName;
import nz.co.nomadconsulting.simplesecurity.util.AnnotatedBeanProperty;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;


/**
 * The IdentityManager is the main api for access to SimpleSecurity. It provides convenient access to the {@link IdentityStore} and also methods
 * {@link #hasRole(Object, Object, Object)} and {@link #hasRole(Object, Object)} to provide basic role based authorisation.
 *
 * @author craig
 *
 */
public class IdentityManager {

    @Inject
    private IdentityStore store;

    @Inject
    private Instance<IdentityStoreConfiguration> configuration;

    private AnnotatedBeanProperty<Roles> rolesProperty;

    private AnnotatedBeanProperty<RoleName> roleNameProperty;

    private AnnotatedBeanProperty<RoleScopeClass> roleScopeClassProperty;

    private AnnotatedBeanProperty<RoleScopeId> roleScopeIdProperty;

    private AnnotatedBeanProperty<RoleGroup> roleGroupProperty;


    @PostConstruct
    public void init() {
        final Class<?> userClass = configuration.get().getUserClass();
        rolesProperty = new AnnotatedBeanProperty<>(userClass, Roles.class);

        final Class<?> roleClass = configuration.get().getRoleClass();
        roleNameProperty = new AnnotatedBeanProperty<>(roleClass,
                RoleName.class);
        roleScopeClassProperty = new AnnotatedBeanProperty<>(roleClass,
                RoleScopeClass.class);
        roleScopeIdProperty = new AnnotatedBeanProperty<>(roleClass,
                RoleScopeId.class);
        roleGroupProperty = new AnnotatedBeanProperty<>(roleClass,
                RoleGroup.class);
    }


    /**
     *
     * @param username
     * @param password
     * @return
     */
    public boolean authenticate(final String username, final String password) {
        return store.authenticate(username, password);
    }


    /**
     *
     * @param username
     * @param password
     * @return
     */
    public boolean createUser(final String username, final String password) {
        return store.createUser(username, password);
    }


    /**
     *
     * @param name
     * @return
     */
    public boolean deleteUser(final String name) {
        return store.deleteUser(name);
    }
    
    
    
    public boolean userExists(final String name) {
       return store.lookupUser(name) != null;
    }


    /**
     *
     * @param user
     * @param role
     */
    public void grantRole(final Object user, final Object role) {
        store.grantRole(user, role);
    }


    /**
     *
     * @param user
     * @param role
     * @param scope
     */
    public void grantRole(final String user, final String role, final Object scope) {
        store.grantRole(user, role, scope);
    }


    /**
     *
     * @param user
     * @param role
     */
    public void revokeRole(final String user, final String role) {
        store.revokeRole(user, role, null);
    }

    
    /**
     * 
     * @param user
     * @param role
     * @param scope
     */
    public void revokeRole(final String user, final String role, final Object scope) {
        store.revokeRole(user, role, scope);
    }
    
    
    /**
     * 
     * @param user
     * @param role
     * @param scope
     */
    public void revokeRole(final String user, final Object role, final Object scope) {
        store.revokeRole(user, role, scope);
    }


    /**
     *
     * @param user
     * @param requestedRole
     * @return
     */
    public boolean hasRole(final Object user, final Object requestedRole) {
        return hasRole(user, requestedRole, null);
    }


    /**
     *
     * @param user
     * @param requestedRole
     * @param scope
     * @return
     */
    public boolean hasRole(final Object user, final Object requestedRole, final Object scope) {
        if (user == null) {
            return false;
        }
        final Collection<?> roles = (Collection<?>) rolesProperty
                .getValue(user);
        for (final Object usersRole : roles) {
            if (checkRole(usersRole, scope, requestedRole)) {
                return true;
            }
        }
        return false;
    }
    
    
    public Set<?> listRoles() {
        return store.getAllRoles();
    }


    protected boolean checkRole(final Object usersRole, final Object scope, final Object requestedRole) {
        if (isRoleEqual(usersRole, requestedRole)
                && isScopeEqual(usersRole, scope)) {
            return true;
        }
        if (roleGroupProperty.isSet()) {
            final Collection<?> roleGroups = (Collection<?>) roleGroupProperty
                    .getValue(usersRole);
            if (roleGroups != null) {
                for (final Object object : roleGroups) {
                    if (checkRole(object, scope, requestedRole)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isRoleEqual(final Object usersRole, final Object requestedRole) {
        if (usersRole.getClass().equals(requestedRole.getClass())) {
            return roleNameProperty.getValue(usersRole).equals(roleNameProperty.getValue(requestedRole));
        }
        return roleNameProperty.getValue(usersRole).equals(requestedRole);
    }


    private boolean isScopeEqual(final Object usersRole, final Object scope) {
        if (!roleScopeClassProperty.isSet()) {
            return true;
        }
        final Object usersScopeClass = roleScopeClassProperty.getValue(usersRole);
        final Object usersScopeId = roleScopeIdProperty.getValue(usersRole); // TODO this might not be set
        // TODO I think this might be broken for revoke roles now.
        return scopeUnset(usersScopeClass, usersScopeId)  || (scopeClassMatches(scope, usersScopeClass) && scopeIdentifierMatches(scope, usersScopeId));
    }


    private boolean scopeUnset(final Object usersScopeClass, final Object usersScopeId) {
        return usersScopeClass == null && usersScopeId == null;
    }


    private boolean scopeIdentifierMatches(final Object scope, final Object usersScopeId) {
        final Object identifier = store.getIdentifier(scope);
        if (identifier.getClass().equals(usersScopeId.getClass())) {
            return usersScopeId.equals(identifier);
        }
        return usersScopeId.toString().equals(identifier.toString());
    }


    private boolean scopeClassMatches(final Object scope, final Object usersScopeClass) {
        if (scope == null || usersScopeClass == null) { 
            return false;
        }
        return usersScopeClass.equals(scope.getClass());
    }
}
