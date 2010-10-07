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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.server.domain.Nonce;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for Nonce objects.
 *
 * @author past
 */
public class NonceDAO extends DAO<Nonce, Long> {
	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a UserDAO object with the provided datastore.
	 */
	@Inject
	public NonceDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Find the nonce object for the specified encoded nonce, that should be
	 * associated with the specified user.
	 *
	 * @param nonce the issued nonce in Base64 encoding
	 * @param userId the ID of the user for whom this nonce should have been issued
	 * @return the retrieved nonce object
	 * @throws ObjectNotFoundException if the nonce or user were not found
	 */
	public Nonce get(String nonce, Long userId) throws ObjectNotFoundException {
		Nonce result = ds.find(Nonce.class, "encodedNonce", nonce).filter("userId", userId).get();
		if (result == null)
			throw  new ObjectNotFoundException("No nonce found");
		return result;
	}
}
