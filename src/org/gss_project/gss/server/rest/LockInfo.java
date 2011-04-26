/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gss_project.gss.server.rest;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Holds a lock information.
 */
class LockInfo {


	// -------------------------------------------------------- Constructor


	/**
	 * Constructor.
	 */
	public LockInfo() {

	}


	// ------------------------------------------------- Instance Variables


	/**
	 *
	 */
	String path = "/";
	/**
	 *
	 */
	String type = "write";
	/**
	 *
	 */
	String scope = "exclusive";
	/**
	 *
	 */
	int depth = 0;
	/**
	 *
	 */
	String owner = "";
	/**
	 *
	 */
	Vector<String> tokens = new Vector<String>();
	/**
	 *
	 */
	long expiresAt = 0;
	/**
	 *
	 */
	Date creationDate = new Date();


	// ----------------------------------------------------- Public Methods


	/**
	 * Get a String representation of this lock token.
	 */
	@Override
	public String toString() {

		String result =  "Type:" + type + "\n";
		result += "Scope:" + scope + "\n";
		result += "Depth:" + depth + "\n";
		result += "Owner:" + owner + "\n";
		result += "Expiration:"
			+ FastHttpDateFormat.formatDate(expiresAt, null) + "\n";
		Enumeration tokensList = tokens.elements();
		while (tokensList.hasMoreElements())
			result += "Token:" + tokensList.nextElement() + "\n";
		return result;

	}


	/**
	 * Return true if the lock has expired.
	 * @return true if the lock has expired.
	 */
	public boolean hasExpired() {
		return System.currentTimeMillis() > expiresAt;
	}


	/**
	 * Return true if the lock is exclusive.
	 * @return true if the lock is exclusive.
	 */
	public boolean isExclusive() {

		return scope.equals("exclusive");

	}


	/**
	 * Get an XML representation of this lock token. This method will
	 * append an XML fragment to the given XML writer.
	 * @param generatedXML the generated XML writer
	 */
	public void toXML(XMLWriter generatedXML) {

		generatedXML.writeElement(null, "activelock", XMLWriter.OPENING);

		generatedXML.writeElement(null, "locktype", XMLWriter.OPENING);
		generatedXML.writeElement(null, type, XMLWriter.NO_CONTENT);
		generatedXML.writeElement(null, "locktype", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "lockscope", XMLWriter.OPENING);
		generatedXML.writeElement(null, scope, XMLWriter.NO_CONTENT);
		generatedXML.writeElement(null, "lockscope", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "depth", XMLWriter.OPENING);
		if (depth == Webdav.INFINITY)
			generatedXML.writeText("Infinity");
		else
			generatedXML.writeText("0");
		generatedXML.writeElement(null, "depth", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "owner", XMLWriter.OPENING);
		generatedXML.writeText(owner);
		generatedXML.writeElement(null, "owner", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "timeout", XMLWriter.OPENING);
		long timeout = (expiresAt - System.currentTimeMillis()) / 1000;
		generatedXML.writeText("Second-" + timeout);
		generatedXML.writeElement(null, "timeout", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "locktoken", XMLWriter.OPENING);
		Enumeration tokensList = tokens.elements();
		while (tokensList.hasMoreElements()) {
			generatedXML.writeElement(null, "href", XMLWriter.OPENING);
			generatedXML.writeText("opaquelocktoken:"
						+ tokensList.nextElement());
			generatedXML.writeElement(null, "href", XMLWriter.CLOSING);
		}
		generatedXML.writeElement(null, "locktoken", XMLWriter.CLOSING);

		generatedXML.writeElement(null, "activelock", XMLWriter.CLOSING);

	}


}