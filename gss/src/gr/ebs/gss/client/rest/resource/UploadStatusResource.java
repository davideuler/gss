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
package gr.ebs.gss.client.rest.resource;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


/**
 * @author kman
 *
 */
public class UploadStatusResource extends RestResource{
	long bytesTransferred;
	long fileSize;

	/**
	 * @param path
	 */
	public UploadStatusResource(String path) {
		super(path);
	}


	/**
	 * Retrieve the bytesTransferred.
	 *
	 * @return the bytesTransferred
	 */
	public long getBytesTransferred() {
		return bytesTransferred;
	}

	/**
	 * Modify the bytesTransferred.
	 *
	 * @param bytesTransferred the bytesTransferred to set
	 */
	public void setBytesTransferred(long bytesTransferred) {
		this.bytesTransferred = bytesTransferred;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int percent(){
		return new Long(bytesTransferred*100/fileSize).intValue();
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		if(json.get("bytesTotal") != null)
			fileSize = new Long(json.get("bytesTotal").toString());
		if(json.get("bytesUploaded") != null)
			bytesTransferred = new Long(json.get("bytesUploaded").toString());

	}

}
