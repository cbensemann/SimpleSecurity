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
package nz.co.nomadconsulting.simplesecurity.authorisation;

import nz.co.nomadconsulting.simplesecurity.Identity;
import nz.co.nomadconsulting.simplesecurity.annotation.Authorised;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;


@Interceptor
@Authorised({})
public class AuthorisationInterceptor {

    private final Logger logger = Logger
            .getLogger(AuthorisationInterceptor.class.getName());

    @Inject
    private Identity identity;


    @AroundInvoke
    public Object authorisationCheck(final InvocationContext ctx)
            throws Exception {

        logger.info(">>> authorisationCheck - LoggingInterceptor - before method invoke: "
                + ctx.getMethod().getName());

        if (!identity.hasRole(ctx.getMethod().getAnnotation(Authorised.class)
                .value()[0])) {
            logger.info("Call made to routine when not privledged (Superuser)");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        logger.info("<<<authorisationCheck - LoggingInterceptor - after method invoke: "
                + ctx.getMethod().getName());

        return ctx.proceed();
    }
}
