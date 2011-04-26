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
package org.gss_project.gss.web.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.DateUtil;


/**
 * @author past
 *
 */
public class TestClient {

	public static void main(String[] args) {
		String user = "ebstest@grnet-hq.admin.grnet.gr";
		String token="PcxaZ/4oIqCqIvCYgsUcKr1hAFcsW40G3kcWJSRPJV5GjzoNuo8RsA==";
		String host = "pithos.grnet.gr";
		String restPath = "/pithos/rest";
		String path = "/" + user + "/files/";
		String now = DateUtil.formatDate(new Date());
		String signature = sign("GET", now, path, token);
		HttpClient client = new HttpClient();
		HostConfiguration hostconfig = new HostConfiguration();
		hostconfig.setHost(host);
		HttpMethod method = new GetMethod(restPath + path);
		Collection<Header> headers = new ArrayList<Header>();
		Header auth = new Header("Authorization", user + " " + signature);
		headers.add(auth);
		Header date = new Header("X-GSS-Date", now);
		headers.add(date);
		System.out.println(headers.toString());
		hostconfig.getParams().setParameter("http.default-headers", headers);
		try {
			// Execute the method.
			int statusCode = client.executeMethod(hostconfig, method);

			if (statusCode != HttpStatus.SC_OK)
				System.err.println("Method failed: " + method.getStatusLine());

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			System.out.println(new String(responseBody));
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	public static String sign(String httpMethod, String timestamp,
				String path, String token) {
		String input = httpMethod + timestamp + path;
		String signed = null;

		try {
			System.err.println("Token:" + token);
			// Get an HMAC-SHA1 key from the authentication token.
			System.err.println("Input: " + input);
			SecretKeySpec signingKey = new SecretKeySpec(Base64.decodeBase64(token.getBytes()), "HmacSHA1");

			// Get an HMAC-SHA1 Mac instance and initialize with the signing key.
			Mac hmac = Mac.getInstance("HmacSHA1");
			hmac.init(signingKey);

			// Compute the HMAC on the input data bytes.
			byte[] rawMac = hmac.doFinal(input.getBytes());

			// Do base 64 encoding.
			signed = new String(Base64.encodeBase64(rawMac), "US-ASCII");

		} catch (InvalidKeyException ikex) {
			System.err.println("Fatal key exception: " + ikex.getMessage());
			ikex.printStackTrace();
		} catch (UnsupportedEncodingException ueex) {
			System.err.println("Fatal encoding exception: "
						+ ueex.getMessage());
		} catch (NoSuchAlgorithmException nsaex) {
			System.err.println("Fatal algorithm exception: "
						+ nsaex.getMessage());
			nsaex.printStackTrace();
		}

		if (signed == null)
			System.exit(-1);
		System.err.println("Signed: " + signed);
		return signed;
	}
}
