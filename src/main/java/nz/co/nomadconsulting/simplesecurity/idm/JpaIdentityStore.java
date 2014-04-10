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
import nz.co.nomadconsulting.simplesecurity.util.BinTools;
import nz.co.nomadconsulting.simplesecurity.util.PasswordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;


/**
 * An implementation of an {@link IdentityStore} using JPA to access a database. <br/>
 * This implementation requries that you provide configuration using an {@link IdentityStoreConfiguration}. This can be done using an application
 * scoped bean like the following:
 * 
 * <pre>
 * &#064;ApplicationScoped
 * public class ConfigurationProducer {
 * 
 *     private IdentityStoreConfiguration configuration;
 * 
 * 
 *     &#064;PostConstruct
 *     public void init() {
 *         configuration = new IdentityStoreConfiguration();
 *         configuration.setUserClass(User.class);
 *         configuration.setRoleClass(Role.class);
 *     }
 * 
 * 
 *     &#064;Produces
 *     public IdentityStoreConfiguration createConfiguration() {
 *         return configuration;
 *     }
 * }
 * </pre>
 *
 * @author craig
 *
 */
@ApplicationScoped
public class JpaIdentityStore implements IdentityStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Instance<IdentityStoreConfiguration> configuration;

    @Inject
    @LoggedIn
    private Event<IdentityStoreUserEvent> loggedInEvent;

    @Inject
    @UserCreated
    private Event<IdentityStoreUserEvent> userCreatedEvent;

    @Inject
    @UserDeleted
    private Event<IdentityStoreUserEvent> userDeletedEvent;

    @Inject
    @PreDeleteUser
    private Event<IdentityStoreUserEvent> preDeleteUserEvent;

    @Inject
    @PrePersistUser
    private Event<IdentityStoreUserEvent> prePersistUserEvent;

    private AnnotatedBeanProperty<Username> usernameProperty;

    private AnnotatedBeanProperty<PasswordHash> passwordProperty;

    private AnnotatedBeanProperty<PasswordSalt> passwordSaltProperty;

    private AnnotatedBeanProperty<UserEnabled> userEnabledProperty;

    private AnnotatedBeanProperty<Roles> userRolesProperty;

    private AnnotatedBeanProperty<RoleName> roleNameProperty;

    private AnnotatedBeanProperty<RoleScopeClass> roleScopeClassProperty;

    private AnnotatedBeanProperty<RoleGroup> roleGroupProperty;

    private AnnotatedBeanProperty<RoleScopeId> roleScopeIdProperty;

    private Class<?> userClass;

    private Class<?> roleClass;


    @PostConstruct
    public void init() {
        userClass = configuration.get().getUserClass();
        roleClass = configuration.get().getRoleClass();
        usernameProperty = new AnnotatedBeanProperty<>(userClass,
                Username.class);
        passwordProperty = new AnnotatedBeanProperty<>(userClass,
                PasswordHash.class);
        passwordSaltProperty = new AnnotatedBeanProperty<>(userClass,
                PasswordSalt.class);
        userEnabledProperty = new AnnotatedBeanProperty<>(userClass,
                UserEnabled.class);
        userRolesProperty = new AnnotatedBeanProperty<>(userClass,
                Roles.class);
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
     * Attempts to authenticate the given user based on the supplied password. <br/>
     * If the user is successfully authenticated an {@link IdentityStoreUserEvent} is fired providing access to the full user object retrieved from
     * the database during authentication. The event is qualified with {@link LoggedIn}
     *
     * @param username
     *            - username corresponding to the {@link Username} annotation on your user class
     * @param password
     *            - plain text password as entered by the user on the screen
     *
     * @return true if the user/password match otherwise false.
     */
    @Override
    public boolean authenticate(final String username, final String password) {
        final Object user = lookupUser(username);
        if (user == null || userEnabledProperty.isSet()
                && (Boolean) userEnabledProperty.getValue(user) == false) {
            return false;
        }

        String passwordHash = null;

        if (passwordSaltProperty.isSet()) {
            final String encodedSalt = (String) passwordSaltProperty
                    .getValue(user);
            if (encodedSalt == null) {
                throw new IdentityManagementException(
                        "A @PasswordSalt property was found on entity " + user
                        + ", but it contains no value");
            }

            passwordHash = generatePasswordHash(password,
                    BinTools.hex2bin(encodedSalt));
        }
        else {
            // TODO throw exception or something here
        }

        final boolean success = slowEquals(passwordHash.toCharArray(), ((String) passwordProperty.getValue(user)).toCharArray());

        if (success) {
            loggedInEvent.fire(new IdentityStoreUserEvent(user));
        }

        return success;
    }


    private boolean slowEquals(final char[] a, final char[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }


    private String generatePasswordHash(final String password, final byte[] salt) {
        try {
            return new PasswordUtils().createPasswordKey(
                    password.toCharArray(), salt, passwordProperty
                    .getAnnotation().iterations());
        }
        catch (final GeneralSecurityException ex) {
            throw new IdentityManagementException(
                    "Exception generating password hash", ex);
        }
    }


    /**
     * Attempts to create a new instance of the user class and sets the username and hashed password/salt. Events will be fired prior to persisting
     * the user {@link IdentityStoreUserEvent} with a qualifier of {@link PrePersistUser}. This allows you to populate other fields on the object.
     * After successful creation/persisting another event is fired {@link IdentityStoreUserEvent} with a qualifier of {@link UserCreated}.
     *
     * @param username
     *            - username corresponding to the {@link Username} annotation on your user class
     * @param password
     *            - plain text password as entered by the user on the screen
     *
     * @return true if the user could be created otherwise false
     */
    @Override
    public boolean createUser(final String username, final String password) {
        try {
            if (userClass == null) {
                throw new IdentityManagementException(
                        "Could not create account, userClass not set");
            }

            if (userExists(username)) {
                throw new IdentityManagementException(
                        "Could not create account, already exists");
            }

            final Object user = userClass.newInstance();

            usernameProperty.setValue(user, username);

            if (password == null) {
                if (userEnabledProperty.isSet()) {
                    userEnabledProperty.setValue(user, false);
                }
            }
            else {
                setUserPassword(user, password);
                if (userEnabledProperty.isSet()) {
                    userEnabledProperty.setValue(user, true);
                }
            }

            prePersistUserEvent.fire(new IdentityStoreUserEvent(user));

            persistEntity(user);

            userCreatedEvent.fire(new IdentityStoreUserEvent(user));

            return true;
        }
        catch (final Exception ex) {
            if (ex instanceof IdentityManagementException) {
                throw (IdentityManagementException) ex;
            }
            else {
                throw new IdentityManagementException(
                        "Could not create account", ex);
            }
        }
    }


    private boolean userExists(final String username) {
        return lookupUser(username) != null;
    }


    protected void persistEntity(final Object user) {
        entityManager.persist(user);
    }


    protected void setUserPassword(final Object user, final String password) {
        final byte[] salt = new PasswordUtils().generateRandomSalt();
        passwordSaltProperty.setValue(user, BinTools.bin2hex(salt));
        passwordProperty.setValue(user, generatePasswordHash(password, salt));
    }


    /**
     * Attempts a {@link #lookupUser(String)} based on the username and removes the entity if found. Event fired prior to deletion -
     * {@link IdentityStoreUserEvent} with a qualifier of {@link PreDeleteUser} and after successful deletion - {@link IdentityStoreUserEvent} with a
     * qualifier of {@link UserDeleted}
     *
     * @param username
     *            - username corresponding to the {@link Username} annotation on your user class
     * 
     * @return true if the user has been deleted otherwise false
     */
    @Override
    public boolean deleteUser(final String username) {
        final Object user = lookupUser(username);
        if (user == null) {
            throw new NoSuchUserException("Could not delete, user '" + username
                    + "' does not exist");
        }

        preDeleteUserEvent.fire(new IdentityStoreUserEvent(user));
        removeEntity(user);
        userDeletedEvent.fire(new IdentityStoreUserEvent(user));

        return true;
    }


    protected void removeEntity(final Object user) {
        // TODO will this work?
        entityManager.remove(user);
    }


    protected Object lookupUser(final String name) {
        try {
            final Object user = entityManager
                    .createQuery(
                            "select u from "
                                    + configuration.get().getUserClass()
                                    .getName() + " u where "
                                    + usernameProperty.getName()
                                    + " = :username")
                                    .setParameter("username", name).getSingleResult();

            return user;
        }
        catch (final NoResultException ex) {
            return null;
        }
    }


    @Override
    public void grantRole(final Object user, final Object role) {
        // grantRole(user, role, null);
    }


    @Override
    public void grantRole(final String username, final String rolename, final Object scope) {
        final Object user = lookupUser(username);

        if (user == null) {
            return; // maybe throw an exception here?
        }

        Object role;
        if (scope == null) {
            // assume role already exists
            role = lookupRole(rolename);
            // TODO throw an exception if role doesn't exist
        }
        else {
            role = createRole(rolename, scope);
        }
        addRoleToUser(user, role);
        persistEntity(user);
    }


    private void addRoleToUser(final Object user, final Object roleToGrant) {
        Collection userRoles = (Collection) userRolesProperty.getValue(user);

        if (userRoles == null) {
            final Type propType = userRolesProperty.getPropertyType();
            Class collectionType;

            if (propType instanceof Class && Collection.class.isAssignableFrom((Class) propType)) {
                collectionType = (Class) propType;
            }
            else if (propType instanceof ParameterizedType &&
                    Collection.class.isAssignableFrom((Class) ((ParameterizedType) propType).getRawType())) {
                collectionType = (Class) ((ParameterizedType) propType).getRawType();
            }
            else {
                throw new IllegalStateException("Could not determine collection type for user roles.");
            }

            // This should either be a Set, or a List...
            if (Set.class.isAssignableFrom(collectionType)) {
                userRoles = new HashSet();
            }
            else if (List.class.isAssignableFrom(collectionType)) {
                userRoles = new ArrayList();
            }

            userRoles.add(roleToGrant);
            userRolesProperty.setValue(user, userRoles);
        }
        else if (!userRoles.contains(roleToGrant)) {
            userRoles.add(roleToGrant);
        }
    }


    // TODO a Scope object could also be provided and used by the end user if they didn't want to 'pollute' their model
    // TODO can this IdentityStore just be used to manage roles and not users?
    protected Object createRole(final String rolename, final Object scope) {
        Object newInstance;
        try {
            newInstance = roleClass.newInstance();
            roleNameProperty.setValue(newInstance, rolename);
            if (roleScopeClassProperty.isSet()) {
                roleScopeClassProperty.setValue(newInstance, scope.getClass());
            }
            // TODO need to deal with classcastexception of identifier
            roleScopeIdProperty.setValue(newInstance, getIdentifier(scope));
            return newInstance;
        }
        catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    private Object getIdentifier(final Object scope) {
        final Class<? extends Object> scopeClass = scope.getClass();
        final String idProperty = getIdProperty(scopeClass);
        Object identifier;
        if (idProperty == null) {
            identifier = scope.toString();
        }
        else {
            try {
                final Field field = scopeClass.getField(idProperty);
                identifier = field.get(scope);
            }
            catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException("TODO i should be a proper exception");
            }
        }
        return identifier;
    }


    private String getIdProperty(final Class<?> entityClass) {
        String idProperty = null;
        final Metamodel metamodel = entityManager.getMetamodel();
        final EntityType entity = metamodel.entity(entityClass);
        final Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
        for (final SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                idProperty = singularAttribute.getName();
                break;
            }
        }

        return idProperty;
    }


    protected Object lookupRole(final String role) {
        try {
            final Object value = entityManager.createQuery(
                    "select r from " + roleClass.getName() + " r where " + roleNameProperty.getName() +
                    " = :role")
                    .setParameter("role", role)
                    .getSingleResult();

            return value;
        }
        catch (final NoResultException ex) {
            return null;
        }
    }


    @Override
    public void revokeRole(final String username, final String rolename, final Object scope) {
        final Object user = lookupUser(username);

        if (user != null) {
            final Object role = lookupRole(rolename);
            final Collection roles = (Collection) userRolesProperty.getValue(user);
            roles.remove(role);
            // TODO what if Role class doesn't implement equals? need to do manual comparison including scope
        }
    }
}
