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
package gr.ebs.gss.server.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
	private static final int TRANSACTION_RETRIES = 20;

	/**
	 * The minimum retry timeout in milliseconds.
	 */
//	private static final int MIN_TIMEOUT = 200;

	/**
	 * Execute the supplied command until it completes, ignoring transaction
	 * rollbacks. Try at least TRANSACTION_RETRIES times before giving up,
	 * each time waiting a random amount of time, using an exponential
	 * backoff scheme. See http://en.wikipedia.org/wiki/Exponential_backoff
	 * for the basic idea.
	 *
	 * @param command the command to execute
	 * @return the value returned by the command
	 * @throws Exception any other exception thrown by the command
	 */
	public T tryExecute(final Callable<T> command) throws Exception {
		T returnValue = null;
		// Schedule a Future task to call the command after delay milliseconds.
		int delay = 0;
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		for (int i = 0; i < TRANSACTION_RETRIES; i++) {
//			final int retry = i;
			ScheduledFuture<T> future = executor.schedule(new Callable<T>() {

				@Override
				public T call() throws Exception {
					return command.call();
				}
			}, delay, TimeUnit.MILLISECONDS);

			try {
				returnValue = future.get();
				break;
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				// XXX: what is the proper check here?
				//if (!(cause instanceof EJBTransactionRolledbackException) ||
				//			retry == TRANSACTION_RETRIES - 1) {
					logger.info("Transaction retry #" + (i+1) +
								" failed due to " + cause, e);
					executor.shutdownNow();
					if (cause instanceof Exception)
						throw (Exception) cause;
					if (cause instanceof Error)
						throw (Error) cause;
				/*}
				delay = MIN_TIMEOUT + (int) (MIN_TIMEOUT * Math.random() * (i + 1));
				String origCause = cause.getCause() == null ?
							cause.getClass().getName() :
							cause.getCause().getClass().getName();
				logger.info("Transaction retry #" + (i+1) + " scheduled in " + delay +
							" msec due to " + origCause);*/
			}

		}
		executor.shutdownNow();
		return returnValue;
	}
}