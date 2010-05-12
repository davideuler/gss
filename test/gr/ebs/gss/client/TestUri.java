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
package gr.ebs.gss.client;

import junit.framework.TestCase;


/**
 * @author natasa
 *
 */
public class TestUri extends TestCase {
	public void testUri(){
		String currentURI = "#http://127.0.0.1:8080/pithos/rest/past@ebs.gr/files/.sync/ Downloads";
		String path = "#http://127.0.0.1:8080/pithos/rest/past@ebs.gr/";
		System.out.println(path.lastIndexOf("/"));
		System.out.println(currentURI.substring(46));
	}

}
