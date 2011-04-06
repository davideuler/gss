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

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.server.domain.Invitation;
import gr.ebs.gss.server.domain.User;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles coupons for quota upgrades.
 *
 * @author past
 */
public class CouponHandler extends BaseServlet {
	/**
	 * The request parameter name for the coupon code.
	 */
	private static final String CODE_PARAM = "code";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(CouponHandler.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("coupon.jsp");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter(CODE_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (code == null || code.isEmpty()) {
			response.sendRedirect("coupon.jsp?error=" + encode("No code was specified"));
			return;
		}

		try {
			Invitation invite = getService().findInvite(code);
			if (invite == null) {
				response.sendRedirect("coupon.jsp?code=&error=" + encode("The specified code was not found"));
				return;
			}
			String firstname = invite.getFirstname() == null? "": invite.getFirstname();
			String lastname = invite.getLastname() == null? "": invite.getLastname();
			String email = invite.getEmail() == null? "": invite.getEmail();

			Object usernameAttr = request.getAttribute("REMOTE_USER");
			if (getConfiguration().getString("testUsername") != null)
				usernameAttr = getConfiguration().getString("testUsername");
			if (usernameAttr == null) {
				Object nameAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_DISPLAYNAME");
				Object givennameAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_GIVENNAME"); // Multi-valued
				Object cnAttr = request.getAttribute("HTTP_SHIB_PERSON_COMMONNAME"); // Multi-valued
				Object snAttr = request.getAttribute("HTTP_SHIB_PERSON_SURNAME"); // Multi-valued
				Object mailAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_MAIL"); // Multi-valued
				Object userclassAttr = request.getAttribute("HTTP_SHIB_EP_UNSCOPEDAFFILIATION"); // Multi-valued
				String authErrorUrl = "authenticationError.jsp";
				authErrorUrl += "?name=" + (nameAttr==null? "-": nameAttr.toString());
				authErrorUrl += "&givenname=" + (givennameAttr==null? "-": givennameAttr.toString());
				authErrorUrl += "&sn=" + (snAttr==null? "-": snAttr.toString());
				authErrorUrl += "&cn=" + (cnAttr==null? "-": cnAttr.toString());
				authErrorUrl += "&mail=" + (mailAttr==null? "-": mailAttr.toString());
				authErrorUrl += "&userclass=" + (userclassAttr==null? "-": userclassAttr.toString());
				response.sendRedirect(authErrorUrl);
				return;
			}
			String username = decodeAttribute(usernameAttr);
			User user;
			try {
				user = getService().findUser(username);
				if (user == null) {
					handleException(response, encode("Your user account was not found!"));
					return;
				}
			} catch (RpcException e) {
				String error = "An error occurred while communicating with the service";
				logger.error(error, e);
				handleException(response, encode(error));
				return;
			}
			response.sendRedirect("couponSubmission.jsp?firstname=" +
					encode(firstname) +	"&lastname=" + encode(lastname) +
					"&email=" + encode(email) + "&code=" + encode(code) +
					"&username=" + encode(username));
		} catch (RpcException e) {
			logger.error(e);
			handleException(response, encode("An error occurred while communicating with the service"));
		}
	}

	private void handleException(HttpServletResponse response, String error) throws IOException {
		String errorUrl = "coupon.jsp?error=" + error;
		response.sendRedirect(errorUrl);
	}
}
