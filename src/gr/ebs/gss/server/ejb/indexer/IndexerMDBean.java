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
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.FileBody;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.ejb.GSSDAO;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Message driven bean that accepts messages whenever a document is created,
 * modified or deleted and adds/removes the item from the search index.
 */
//@MessageDriven(activationConfig={@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
//									@ActivationConfigProperty(propertyName="destination", propertyValue="queue/gss-indexingQueue")})
//@ResourceAdapter("hornetq-ra.rar")
public class IndexerMDBean implements MessageListener {
	/**
	 * The logger
	 */
	private static final Log logger = LogFactory.getLog(IndexerMDBean.class);

	/**
	 * EJB offering access to the JPA entity manager
	 */
	GSSDAO dao;

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
		PostMethod method = null;
		String idStr = "";
		try {
			MapMessage map = (MapMessage) msg;
			Long id = (Long) map.getObject("id");
			idStr = id.toString();
			boolean delete = map.getBoolean("delete");
			HttpClient httpClient = new HttpClient();
			if (delete) {
				method = new PostMethod(getConfiguration().getString("solrUpdateUrl"));
				String deleteXMLMsg = "<delete><id>" + idStr + "</id></delete>";
				if (logger.isDebugEnabled())
					logger.debug(deleteXMLMsg);
				method.setRequestEntity(new StringRequestEntity(deleteXMLMsg, "text/xml", "iso8859-1"));
				int statusCode = httpClient.executeMethod(method);
				if (logger.isDebugEnabled())
					logger.debug("HTTP status: " + statusCode);
				String response = method.getResponseBodyAsString();
				if (logger.isDebugEnabled())
					logger.debug(response);

				method.releaseConnection();
				if (statusCode != 200)
					throw new EJBException("Response from Solr for deleting file id " +
								idStr + " had status: " + statusCode);
				sendCommit(httpClient, 0);
			} else {
				FileHeader file = dao.getFileForIndexing(id);
				FileBody body = file.getCurrentBody();
				String type = null;
				String mime = body.getMimeType();
				boolean nofile = false;
				if (body.getFileSize() > getConfiguration().getLong("solrDocumentUploadLimitInKB") * 1024)
					nofile = true;
				else if (mime.equals("application/pdf"))
					type = "pdf";
				else if (mime.equals("text/plain"))
					type = "text";
				else if (mime.equals("text/html"))
					type = "html";
				else if (mime.endsWith("msword"))
					type = "doc";
				else if (mime.endsWith("ms-excel"))
					type = "xls";
				else if (mime.endsWith("powerpoint"))
					type = "ppt";
				else
					nofile = true;
				if (!nofile) {
					method = new PostMethod(getConfiguration().getString("solrUpdateRichUrl"));
					List<Part> parts = new ArrayList<Part>();
					parts.add(new StringPart("stream.type", type));
					StringBuffer fieldnames = new StringBuffer("id,name");
					if (!file.getTags().isEmpty())
						fieldnames.append(",tag");
					parts.add(new StringPart("fieldnames", fieldnames.toString()));
					parts.add(new StringPart("id", idStr));
					parts.add(new StringPart("name", tokenizeFilename(file.getName()), "UTF-8"));
					for (String tag : file.getTags())
						parts.add(new StringPart("tag", tag, "UTF-8"));
					parts.add(new StringPart("stream.fieldname", "body"));
					parts.add(new StringPart("commit", "true"));
					parts.add(new FilePart(file.getName(), new File(body.getStoredFilePath())));
					method.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[1]), method.getParams()));
					httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
					if (logger.isDebugEnabled())
						logger.debug("Sending rich document " + idStr);
					int statusCode = httpClient.executeMethod(method);
					if (logger.isDebugEnabled())
						logger.debug("HTTP status: " + statusCode);
					String response = method.getResponseBodyAsString();
					if (logger.isDebugEnabled())
						logger.debug(response);
					if (statusCode != 200)
						throw new EJBException("Response from Solr for updating file id " +
									idStr + " had status: " + statusCode);
				} else {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.newDocument();
					Node root = doc.createElement("add");
					doc.appendChild(root);
					Node docNode = doc.createElement("doc");
					root.appendChild(docNode);
					Element field = doc.createElement("field");
					field.setAttribute("name", "id");
					docNode.appendChild(field);
					field.appendChild(doc.createTextNode(idStr));

					field = doc.createElement("field");
					field.setAttribute("name", "name");
					docNode.appendChild(field);
					field.appendChild(doc.createTextNode(tokenizeFilename(file.getName())));

					for (String tag : file.getTags()) {
						field = doc.createElement("field");
						field.setAttribute("name", "tag");
						docNode.appendChild(field);
						field.appendChild(doc.createTextNode(tag));
					}

					TransformerFactory fact = TransformerFactory.newInstance();
					Transformer trans = fact.newTransformer();
					trans.setOutputProperty(OutputKeys.INDENT, "yes");
					StringWriter sw = new StringWriter();
					StreamResult sr = new StreamResult(sw);
					DOMSource source = new DOMSource(doc);
					trans.transform(source, sr);
					if (logger.isDebugEnabled())
						logger.debug(sw.toString());

					method = new PostMethod(getConfiguration().getString("solrUpdateUrl"));
					method.setRequestEntity(new StringRequestEntity(sw.toString(),"text/xml", "UTF-8"));
					int statusCode = httpClient.executeMethod(method);
					if (logger.isDebugEnabled())
						logger.debug("HTTP status: " + statusCode);
					String response = method.getResponseBodyAsString();
					if (logger.isDebugEnabled())
						logger.debug(response);

					method.releaseConnection();
					if (statusCode != 200)
						throw new EJBException("Response from Solr for updating file id " +
									idStr + " had status: " + statusCode);

					sendCommit(httpClient, 0);
				}
			}
		}
		catch (JMSException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (UnsupportedEncodingException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (HttpException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (IOException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (ObjectNotFoundException e) {
			logger.warn("Error processing file ID " + idStr + ": Indexing " +
					"aborted because the file could not be found");
		} catch (ParserConfigurationException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (TransformerConfigurationException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		} catch (TransformerException e) {
			throw new EJBException("Error processing file ID " + idStr, e);
		}
		finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	/**
	 * Sends a commit message to the solr server
	 *
	 * @param httpClient
	 * @param retryCount If the commit fails, it is retried three times. This parameter is passed in the recursive
	 * 					calls to stop the recursion
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws HttpException
	 */
	private void sendCommit(HttpClient httpClient, int retryCount) throws UnsupportedEncodingException, IOException, HttpException {
		PostMethod method = null;
		try {
			if (logger.isDebugEnabled())
				logger.debug("Commit retry: " + retryCount);
			method = new PostMethod(getConfiguration().getString("solrUpdateUrl"));
			method.setRequestEntity(new StringRequestEntity("<commit/>", "text/xml", "iso8859-1"));
			int statusCode = httpClient.executeMethod(method);
			if (logger.isDebugEnabled())
				logger.debug("HTTP status: " + statusCode);
			String response = method.getResponseBodyAsString();
			if (logger.isDebugEnabled())
				logger.debug(response);
			if (statusCode != 200 && retryCount < 2) {
				try {
					Thread.sleep(10000); // Give Solr a little time to be available.
				} catch (InterruptedException e) {
				}
				sendCommit(httpClient, retryCount + 1);
			}
		}
		finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private String tokenizeFilename(String filename){
		StringBuffer result = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(filename,"._");
		while(tokenizer.hasMoreTokens()){
			result.append(tokenizer.nextToken());
			result.append(" ");
		}
		result.append(filename);
		return result.toString();
	}
}
