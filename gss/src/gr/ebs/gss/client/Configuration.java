/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client;

import com.google.gwt.i18n.client.Constants;


/**
 * This interface contains bindings for the compile-time configurable
 * entities of the application.
 *
 * @author past
 */
public interface Configuration extends Constants {
	/**
	 * @return the name of the service
	 */
	@DefaultStringValue("GSS")
	String serviceName();

	/**
	 * @return the login URL
	 */
	@DefaultStringValue("/gss/login")
	String loginUrl();

	/**
	 * @return the logout URL
	 */
	@DefaultStringValue("/gss/login")
	String logoutUrl();

	/**
	 * @return the authentication cookie name
	 */
	@DefaultStringValue("_gss_a")
	String authCookie();

	/**
	 * @return the separator string between username and token in the
	 * authentication cookie
	 */
	@DefaultStringValue("|")
	String cookieSeparator();

	/**
	 * @return the relative path of the API root URL
	 */
	@DefaultStringValue("rest/")
	String apiPath();

	/**
	 * @return the WebDAV URL
	 */
	@DefaultStringValue("/webdav/")
	String webdavUrl();

}
