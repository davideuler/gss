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
package gr.ebs.gss.client.rest;


/**
 * @author kman
 *
 */
public class RestException extends Throwable{

	private int httpStatusCode;
    private String httpStatusText;
    private String text;
    private String path;
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

    public RestException(String path, int httpStatusCode, String httpStatusText, String text) {
            super("HTTP error: " + httpStatusCode+"\nPapth:"+path + "\nStatus text:" + httpStatusText + "\nText:" + text);
            this.httpStatusCode = httpStatusCode;
            this.httpStatusText = httpStatusText;
            this.path = path;
            this.text = text;
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
