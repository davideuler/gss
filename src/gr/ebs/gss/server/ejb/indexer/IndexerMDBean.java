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
package gr.ebs.gss.server.ejb.indexer;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.common.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.jboss.ejb3.annotation.ResourceAdapter;

/**
 * Message driven bean that accepts messages whenever a document is created,
 * modified or deleted and adds/removes the item from the search index.
 */
@MessageDriven(activationConfig={@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
									@ActivationConfigProperty(propertyName="destination", propertyValue="queue/gss-indexingQueue")})
@ResourceAdapter("hornetq-ra.rar")
public class IndexerMDBean implements MessageListener {
	/**
	 * The logger
	 */
	private static final Log logger = LogFactory.getLog(IndexerMDBean.class);

	/**
	 * EJB offering access to the JPA entity manager
	 */
	@EJB ExternalAPI service;

	/**
	 * Decides to add or drop an item from the index depending on the message
	 * received
	 *
	 * It currently uses the patched solr API for rich documents. This API does not
	 * allow indexing time field boosting. For this reason we have to use the dismax search API (instead of the
	 * standard) that allows for search time field boosting
	 *
	 * @param msg
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message msg) {
		Long id = null;
		try {
			MapMessage map = (MapMessage) msg;
			id = (Long) map.getObject("id");
			boolean delete = map.getBoolean("delete");
            if (delete) {
                CommonsHttpSolrServer solr = new CommonsHttpSolrServer(getConfiguration().getString("solr.url"));
				sendDelete(solr, id);
                solr.commit();
			} else {
				service.postFileToSolr(id);
			}
		}
		catch (JMSException e) {
			throw new EJBException("Error processing file ID " + id, e);
		}
		catch (IOException e) {
			throw new EJBException("Error processing file ID " + id, e);
		}
		catch (SolrServerException e) {
			throw new EJBException(e);
		}
        catch (ObjectNotFoundException e) {
            throw new EJBException(e);
        }
    }


	/**
	 * Sends a delete command to solr. The id is the Long id of the indexed document
	 * 
	 * @param solr
	 * @param id
	 * @throws SolrServerException
	 * @throws IOException
	 */
	private void sendDelete(CommonsHttpSolrServer solr, Long id)	throws SolrServerException, IOException {
		solr.deleteById(id.toString());
	}
}
