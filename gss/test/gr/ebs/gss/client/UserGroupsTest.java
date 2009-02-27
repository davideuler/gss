/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.ServiceDefTarget;


/**
 * A test case that verifies the behavior of the retrieval of the groups that
 * belong to a particular user.
 *
 * @author past
 */
public class UserGroupsTest extends GWTTestCase {

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
	 */
	public String getModuleName() {
		return "gr.ebs.gss.GSS";
	}

	/**
	 * Perform the test of getting the groups that belong to a particular user.
	 */
	public void testUserGroups() {
		delayTestFinish(10000);
		/*getRemoteService().getGroups(new Long(1L), new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List groupList = (List) result;
				// Copy the ArrayList to an array, the Java 1.4 way
				final GroupDTO[] g = new GroupDTO[groupList.size()];
				final Iterator iter = groupList.iterator();
				int j = 0;
				while (iter.hasNext())
					g[j++] = (GroupDTO) iter.next();

				assertEquals(g[0].getName(), "EBS");
				assertEquals(g[1].getName(), "Family");
				assertNull(g[2]);
				finishTest();
			}

			public void onFailure(final Throwable caught) {
				fail(caught.toString());
			}
		});*/
	}

	/**
	 * An RPC service proxy.
	 */
	private GSSServiceAsync service;

	/**
	 * Return the service proxy. If this is the first time it is requested, it
	 * is then stored for subsequent requests.
	 *
	 * @return the service proxy
	 */
	private GSSServiceAsync getRemoteService() {
		if (service == null) {
			final GSSServiceAsync freshService = (GSSServiceAsync) GWT.create(GSSService.class);
			final ServiceDefTarget endpoint = (ServiceDefTarget) freshService;
			final String serviceUrl = "http://localhost:8080/gss/gss";
			System.out.println(serviceUrl);
			endpoint.setServiceEntryPoint(serviceUrl);
			service = freshService;
		}
		return service;
	}

}
