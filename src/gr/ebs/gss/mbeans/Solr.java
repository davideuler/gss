/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.mbeans;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.server.ejb.ExternalAPIRemote;

import javax.management.JMRuntimeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.system.ServiceMBeanSupport;


/**
 * @author chstath
 *
 */
public class Solr extends ServiceMBeanSupport implements SolrMBean {
	/* (non-Javadoc)
	 * @see gr.ebs.gss.SolrMBean#rebuildIndex()
	 */
	public void rebuildIndex() {
		try {
			InitialContext ctx = new InitialContext();
			Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
			ExternalAPIRemote service = (ExternalAPIRemote) PortableRemoteObject.narrow(ref, ExternalAPIRemote.class);
			service.rebuildSolrIndex();
		} catch (ClassCastException e) {
			throw new JMRuntimeException(e.getMessage());
		} catch (NamingException e) {
			throw new JMRuntimeException(e.getMessage());
		}
	}
}
