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

public class JpaPermissionStore implements PermissionStore {

    @Override
    public boolean hasPermission(final Object identity, final Object resource, final String permission) {

        return false;
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
