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
package nz.co.nomadconsulting.simplesecurity;

import javax.enterprise.inject.Vetoed;


@Vetoed
public class IdentityStoreConfiguration {

    private Class<?> userClass;

    private Class<?> roleClass;


    public Class<?> getUserClass() {
        return userClass;
    }


    public void setUserClass(final Class<?> userClass) {
        this.userClass = userClass;
    }


    public Class<?> getRoleClass() {
        return roleClass;
    }


    public void setRoleClass(final Class<?> roleClass) {
        this.roleClass = roleClass;
    }
}
