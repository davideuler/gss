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
package gr.ebs.gss.server.configuration;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.server.CacheFilter;
import gr.ebs.gss.server.CouponHandler;
import gr.ebs.gss.server.CouponVerifier;
import gr.ebs.gss.server.Invitations;
import gr.ebs.gss.server.Login;
import gr.ebs.gss.server.NonceIssuer;
import gr.ebs.gss.server.Policy;
import gr.ebs.gss.server.Registration;
import gr.ebs.gss.server.TokenRetriever;
import gr.ebs.gss.server.ejb.AccountingDAO;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.ejb.ExternalAPIBean;
import gr.ebs.gss.server.ejb.FileDAO;
import gr.ebs.gss.server.ejb.FileUploadDAO;
import gr.ebs.gss.server.ejb.FolderDAO;
import gr.ebs.gss.server.ejb.GroupDAO;
import gr.ebs.gss.server.ejb.Transaction;
import gr.ebs.gss.server.ejb.UserClassDAO;
import gr.ebs.gss.server.ejb.UserDAO;
import gr.ebs.gss.server.rest.RequestHandler;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


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
				// Wire the servlets and filters.
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
				// Wire the rest of the objects.
				bind(ExternalAPI.class).to(ExternalAPIBean.class);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected Datastore provideDatastore() throws UnknownHostException, MongoException {
				Mongo mongo = new Mongo(getConfiguration().getString("mongoHost", "localhost"));
				Morphia morphia = new Morphia();
				morphia.mapPackage("gr.ebs.gss.server.domain");
				Datastore ds = morphia.createDatastore(mongo, "gss");
				ds.ensureIndexes();
				ds.ensureCaps();
				return ds;
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected UserDAO provideUserDAO(Datastore ds) {
				return new UserDAO(ds);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected UserClassDAO provideUserClassDAO(Datastore ds) {
				return new UserClassDAO(ds);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected FolderDAO provideFolderDAO(Datastore ds, UserDAO userDao,
					GroupDAO groupDao) {
				return new FolderDAO(ds, userDao, groupDao);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected FileDAO provideFileDAO(Datastore ds, FolderDAO folderDao) {
				return new FileDAO(ds, folderDao);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected AccountingDAO provideAccountingDAO(Datastore ds) {
				return new AccountingDAO(ds);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected FileUploadDAO provideFileUploadDAO(Datastore ds) {
				return new FileUploadDAO(ds);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected GroupDAO provideGroupDAO(Datastore ds) {
				return new GroupDAO(ds);
			}

			@SuppressWarnings("unused")
			@Provides @Singleton
			protected Transaction provideTransaction(UserDAO userDao,
					UserClassDAO userClassDao, FolderDAO folderDao,
					FileDAO fileDao, AccountingDAO accountingDao,
					FileUploadDAO fileUploadDao, GroupDAO groupDao) {
				return new Transaction(userDao, userClassDao, folderDao,
						fileDao, accountingDao, fileUploadDao, groupDao);
			}
		});
	}
}