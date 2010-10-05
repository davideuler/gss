/*
 * Copyright 2008, 2009, 2010 Electronic Business Systems Ltd.
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
package gr.ebs.gss.server.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * The class that holds an issued nonce for a user.
 *
 * @author past
 */
@Entity
public class Nonce {

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Nonce.class);

	/**
	 * The nonce size in bytes.
	 */
	private static final int NONCE_SIZE = 20;

	/**
	 * The persistence ID of the object.
	 * XXX: we must generate unique ids ourselves, if type is not ObjectId,
	 * so we do it in the constructor
	 */
	@Id
	private Long id;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public Nonce() {
		id = new Random().nextLong();
	}

	/**
	 * The nonce issued for logging in this user.
	 */
	private byte[] nonce;

	/**
	 * The nonce encoded in Base64.
	 */
	private String encodedNonce;

	/**
	 * The time that the user's issued nonce
	 * will expire.
	 */
	private Date nonceExpiryDate;


	/**
	 * The ID of the user for whom this nonce was issued.
	 */
	private Long userId;

	/**
	 * Retrieve the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Retrieve the nonce. If it is not valid or non-existent,
	 * this method returns null. Therefore, call sites must
	 * request a regeneration of the authentication token in
	 * both cases.
	 *
	 * @return the nonce
	 */
	public byte[] getNonce() {
		if (isNonceValid())
			return nonce;
		return null;
	}

	/**
	 * Return true if the nonce is usable, or false
	 * if a new one must be regenerated.
	 *
	 * @return true if the nonce is valid
	 */
	private boolean isNonceValid() {
		if (nonce == null)
			return false;
		if (nonceExpiryDate == null)
			return false;
		if (nonceExpiryDate.before(new Date()))
			return false;
		return true;
	}

	/**
	 * Creates a new nonce and resets its expiry date.
	 *
	 * @param userId the ID of the associated user
	 * @return a new nonce
	 */
	public static Nonce createNonce(Long userId) {
		Nonce n = new Nonce();
		n.userId = userId;
		SecureRandom random = new SecureRandom();
		n.nonce = new byte[NONCE_SIZE];
		random.nextBytes(n.nonce);
		Calendar cal = Calendar.getInstance();
		// Set nonce time-to-live to 5 minutes.
		cal.add(Calendar.MINUTE, 5);
		n.nonceExpiryDate = cal.getTime();
		try {
			n.encodedNonce = URLEncoder.encode(new String(Base64.encodeBase64(n.nonce), "US-ASCII"), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return n;
	}

	/**
	 * Retrieve the userId.
	 *
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Modify the nonce.
	 *
	 * @param aNonce the nonce to set
	 */
	public void setNonce(byte[] aNonce) {
		nonce = aNonce;
	}

	/**
	 * Modify the nonceExpiryDate.
	 *
	 * @param aNonceExpiryDate the nonceExpiryDate to set
	 */
	public void setNonceExpiryDate(Date aNonceExpiryDate) {
		nonceExpiryDate = aNonceExpiryDate;
	}

	/**
	 * Retrieve the encodedNonce.
	 *
	 * @return the encodedNonce
	 */
	public String getEncodedNonce() {
		return encodedNonce;
	}

	/**
	 * Retrieve the nonceExpiryDate.
	 *
	 * @return the nonceExpiryDate
	 */
	public Date getNonceExpiryDate() {
		return nonceExpiryDate;
	}

	@Override
	public int hashCode() {
		return 31 + (id == null ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nonce other = (Nonce) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
