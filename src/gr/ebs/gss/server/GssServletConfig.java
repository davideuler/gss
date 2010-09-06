/*
 * Copyright 2010 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.server;

import gr.ebs.gss.server.rest.RequestHandler;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;


/**
 * A servlet context listener that configures Guice.
 *
 * @author past
 */
public class GssServletConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {
				filter("/*").through(CacheFilter.class);
				serve("/login").with(Login.class);
				serve("/policy").with(Policy.class);
				serve("/register").with(Registration.class);
				serve("/invites").with(Invitations.class);
				serve("/coupon").with(CouponHandler.class);
				serve("/submitCoupon").with(CouponVerifier.class);
				serve("/nonce").with(NonceIssuer.class);
				serve("/token").with(TokenRetriever.class);
				Map<String, String> params = new HashMap<String, String>(2);
				params.put("input", "4096");
				params.put("output", "4096");
				serve("/rest/*").with(RequestHandler.class, params);
			}
		});
	}
}