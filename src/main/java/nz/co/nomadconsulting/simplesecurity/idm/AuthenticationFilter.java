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

import nz.co.nomadconsulting.simplesecurity.Identity;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@ApplicationScoped
public abstract class AuthenticationFilter implements Filter {

    @Inject
    private Logger log;

    @Inject
    private Instance<Identity> identityInstance;

    private UrlPatternMatcher urlPatternMatcher;

    private UnAuthenticatedResponse unauthenticatedResponse;


    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException(
                    "This filter can only process HttpServletRequest requests");
        }
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        final Identity identity = identityInstance.get();

        if (identity.isLoggedIn() || isNotSecurePage((HttpServletRequest) request)) {
            chain.doFilter(request, response);
        }
        else {
            unauthenticatedResponse.respond(httpRequest, httpResponse);
        }
    }


    protected boolean isNotSecurePage(final HttpServletRequest request) {
        final StringBuffer urlBuffer = new StringBuffer(request.getServletPath());
//        if (request.getQueryString() != null) {
//            urlBuffer.append("?").append(request.getQueryString());
//        }
        final String requestUri = urlBuffer.toString();
        final boolean matches = urlPatternMatcher.matches(requestUri);
        log.fine("matching incoming requestUri " + requestUri + ": " + matches);
        return matches;
    }


    protected void setUrlPatternMatcher(final UrlPatternMatcher urlPatternMatcher) {
        this.urlPatternMatcher = urlPatternMatcher;
    }
    

    protected void setUnAuthenticatedResponse(final JsfAwareRedirectingUnAuthenticatedResponse responder) {
        unauthenticatedResponse = responder;
    }


    @Override
    public void destroy() {
    }
}
