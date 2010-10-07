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
package gr.ebs.gss.server.service;

import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.User;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for FileUploadStatus objects.
 *
 * @author past
 */
public class FileUploadDAO extends DAO<FileUploadStatus, Long> {
	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a FileUploadDAO object with the provided datastore.
	 */
	@Inject
	public FileUploadDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Retrieve the current status for the file with the specified name,
	 * uploaded by the specified user.
	 */
	public FileUploadStatus getStatus(User user, String filename) {
		return ds.find(FileUploadStatus.class, "owner", user).
				filter("filename", filename).get();
	}
}
