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

import java.util.Set;

/**
 * Provides an abstraction over and access to an underlying persistent store for user data. This is likely to be a database, ldap, or even a
 * filesystem or anything else.
 *
 * @see JpaIdentityStore
 * 
 * @author craig
 *
 */
public interface IdentityStore {

    boolean deleteUser(final String name);


    boolean createUser(final String username, final String password);


    boolean authenticate(final String username, final String password);


    void grantRole(final Object user, final Object role);


    void grantRole(final String username, final String rolename, final Object scope);


    void revokeRole(final String username, final String rolename, final Object scope);


    Set<Object> getAllRoles();


    Object getIdentifier(Object scope);
}
