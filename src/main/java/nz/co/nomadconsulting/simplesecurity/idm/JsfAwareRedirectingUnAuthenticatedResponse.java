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

import nz.co.nomadconsulting.simpleessentials.FacesMessages;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsfAwareRedirectingUnAuthenticatedResponse extends RedirectingUnAuthenticatedResponse{

    @Inject
    private FacesMessages facesMessages;
    
    public JsfAwareRedirectingUnAuthenticatedResponse(final String loginPage) {
        super(loginPage);
    }

    @Override
    public void respond(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        facesMessages.postGlobalMessage("nz.co.nomadconsulting.simplesecurity.idm.JsfAwareRedirectingUnAuthenticatedResponse.message", FacesMessage.SEVERITY_WARN);
        super.respond(request, response);
    }
}
