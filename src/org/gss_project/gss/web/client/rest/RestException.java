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
package org.gss_project.gss.web.client.rest;


/**
 * @author kman
 *
 */
public class RestException extends Throwable {

	private int httpStatusCode;
    private String httpStatusText;
    private String text;

    public RestException() {
    }

    public RestException(String message) {
            super(message);
    }

    public RestException(Throwable innerException) {
            super(innerException);
    }

    public RestException(String message, Throwable innerException) {
            super(message, innerException);
    }

    public RestException(String aPath, int aStatusCode, String aStatusText, String aText) {
            super("HTTP error: " + aStatusCode+"\nPapth:"+aPath + "\nStatus text:" + aStatusText + "\nText:" + aText);
            httpStatusCode = aStatusCode;
            httpStatusText = aStatusText;
            text = aText;
    }

    public int getHttpStatusCode() {
            return httpStatusCode;
    }

    public String getHttpStatusText() {
            return httpStatusText;
    }

    public String getText() {
            return text;
    }

}
