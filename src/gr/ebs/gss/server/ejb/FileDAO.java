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

import gr.ebs.gss.server.domain.FileHeader;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
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
	 * @param userId the ID of the user
	 * @return the total number of files in the user's namespace
	 */
	public long getFileCount(Long userId) {
		return ds.find(FileHeader.class, "owner", userId).countAll();
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
}
