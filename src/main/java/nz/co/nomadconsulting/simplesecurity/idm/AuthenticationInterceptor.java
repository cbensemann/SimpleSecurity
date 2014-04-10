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
import nz.co.nomadconsulting.simplesecurity.annotation.Authenticated;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;


@Interceptor
@Authenticated
public class AuthenticationInterceptor {

    private final Logger logger = Logger
            .getLogger(AuthenticationInterceptor.class.getName());

    @Inject
    private Identity identity;


    @AroundInvoke
    public Object authenticationCheck(final InvocationContext ctx)
            throws Exception {

        logger.info("LoggingInterceptor - before method invoke: "
                + ctx.getMethod().getName());

        if (!identity.isLoggedIn()) {
            logger.info("Call made to routine when not logged in");
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        logger.info("LoggingInterceptor - after method invoke: "
                + ctx.getMethod().getName());

        return ctx.proceed();
    }
}
