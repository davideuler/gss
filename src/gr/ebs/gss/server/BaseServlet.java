/*
 * Copyright 2008, 2009, 2010 Electronic Business Systems Ltd.
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

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Formatter;

import javax.servlet.http.HttpServlet;

import com.google.inject.Inject;

/**
 * The base servlet contains a collection of helper methods.
 *
 * @author past
 */
public class BaseServlet extends HttpServlet {
	/**
	 * The request parameter name for the GWT code server URL, used when
	 * debugging.
	 */
	protected static final String GWT_SERVER_PARAM = "gwt.codesvr";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The injected ExternalAPI service.
	 */
	@Inject
	protected ExternalAPI service;

	/**
	 * Return the name of the service.
	 */
	protected String getServiceName() {
		return getConfiguration().getString("serviceName", "GSS");
	}

	/**
	 * Decode the request attribute provided by the container to a UTF-8
	 * string, since GSS assumes all data to be encoded in UTF-8. The
	 * servlet container's encoding can be specified in gss.properties.
	 */
	protected String decodeAttribute(Object attribute) throws UnsupportedEncodingException {
		return new String(attribute.toString().getBytes(getConfiguration().getString("requestAttributeEncoding")), "UTF-8");
	}

	/**
	 * A helper method that converts a byte buffer to a printable list of
	 * hexadecimal numbers.
	 */
	protected String getHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		for (int i=0; i<buffer.length; i++)
			formatter.format("0x%x, ", buffer[i]);
		return sb.toString();
	}

	protected String encode(String parameter) throws UnsupportedEncodingException {
		return URLEncoder.encode(parameter, "UTF-8");
	}
}
