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
package gr.ebs.gss.server.ejb;

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.User;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;


/**
 * The Data Access Object for files.
 *
 * @author past
 */
public class FileDAO extends DAO<FileHeader, Long> {
	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a FileDAO object with the provided datastore.
	 */
	@Inject
	public FileDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Calculates total file count of user.
	 *
	 * @param user the user
	 * @return the total number of files in the user's namespace
	 */
	public long getFileCount(User user) {
		return ds.find(FileHeader.class, "owner", user).countAll();
	}

	/**
	 * Calculates total file size of user.
	 * TODO: verify the sum
	 *
	 * @param userId the ID of the user
	 * @return the aggregate size of all the user's files
	 */
	public long getFileSize(Long userId) {
		DBObject mapreduce = new BasicDBObject().
				append("mapreduce", collection().getName()).
				append("map", "function() {" +
						"var self = this;" +
						"this.bodies.forEach(function(body)" +
						"emit(self.id + '' + body.version, body.fileSize);" +
						")};" +
						"}").
				append("reduce", "function(k, vals) {" +
						"var sum = 0;" +
						"for (var i in vals) sum += vals[i];" +
						"return sum;" +
						"}").
				append("query", "{ owner: " + userId + "}");
		CommandResult result = ds.getDB().command(mapreduce);
		return result.containsField("value") ? result.getLong("value") : 0;
	}

	/**
	 * Returns a list of files contained in the folder specified by its id.
	 * It ignores files marked as deleted, if the ignoreDeleted flag is true.
	 *
	 * @param folder
	 * @param user
	 * @param ignoreDeleted
	 * @return List<FileHeader>
	 * @throws ObjectNotFoundException
	 */
	public List<FileHeader> getFiles(Folder folder, User user, boolean ignoreDeleted) throws ObjectNotFoundException {
		if (folder == null)
			throw new ObjectNotFoundException("No folder specified");
		if (user == null)
			throw new ObjectNotFoundException("No user specified");

		Query<FileHeader> query = ds.find(FileHeader.class, "folder", folder);
		if(ignoreDeleted)
			query.filter("deleted", false);
		List<FileHeader> tempList = query.asList();
		List<FileHeader> retv = new ArrayList<FileHeader>();
		for (FileHeader f: tempList)
			if (f.hasReadPermission(user)) retv.add(f);
		return retv;
	}
}
