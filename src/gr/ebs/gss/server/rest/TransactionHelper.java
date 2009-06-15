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
package gr.ebs.gss.server.rest;

import java.util.concurrent.Callable;

import javax.ejb.EJBTransactionRolledbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class that provides a method for repeatedly trying a transaction
 * that is rolled back due to optimistic locking exceptions.
 *
 * @author  past
 */
public class TransactionHelper<T> {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(TransactionHelper.class);

	/**
	 * Number of times to retry a transaction that was rolled back due to
	 * optimistic locking.
	 */
	private static final int TRANSACTION_RETRIES = 10;

	/**
	 * Execute the supplied command until it completes, ignoring
	 * EJBTransactionRolledbackException. Try at least TRANSACTION_RETRIES
	 * times before giving up.
	 *
	 * @param command the command to execute
	 * @return the value returned by the command
	 * @throws Exception any other exception thrown by the command
	 */
	public T tryExecute(Callable<T> command) throws Exception {
		T returnValue = null;
		for (int i = 0; i < TRANSACTION_RETRIES; i++) {
			try {
				returnValue = command.call();
				break;
			} catch(EJBTransactionRolledbackException trbe) {
				if (i == TRANSACTION_RETRIES - 1)
					throw trbe;
			} catch (Exception e) {
				throw e;
			}
			logger.debug("Transaction retry: " + (i+1));
		}
		return returnValue;
	}
}