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

import gr.ebs.gss.server.domain.AccountingInfo;
import gr.ebs.gss.server.domain.User;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.code.morphia.DAO;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;


/**
 * The Data Access Object for AccountingInfo objects.
 *
 * @author past
 */
public class AccountingDAO extends DAO<AccountingInfo, Long> {
	private static final int BANDWIDTH_TIME_PERIOD_FIELD = Calendar.MONTH;
	private static final int BANDWIDTH_TIME_PERIOD_AMOUNT = 1;

	/**
	 * The Morphia datastore instance.
	 */
	private final Datastore ds;

	/**
	 * Construct a UserDAO object with the provided datastore.
	 */
	@Inject
	public AccountingDAO(Datastore aStore) {
		super(aStore);
		ds = aStore;
	}

	/**
	 * Update accounting info for given user.
	 * Adds bandwidth used to appropriate time period bucket.
	 * Bucket is created if needed.
	 *
	 * @param user The user to update
	 * @param date Date of transaction
	 * @param bandwidthDiff Bandwidth used; positive for addition,
	 * negative for subtraction (e.g. to rollback)
	 */
	public void updateAccounting(User user, Date date, long bandwidthDiff) {
		AccountingInfo ai = ds.find(AccountingInfo.class, "user", user).
				filter("dateFrom <=", date).
				filter("dateTo >=", date).
				get();
		if (ai == null) {
			// The right entry does not exist; must be created.
			// This is where we set the initial time period.
			// We now start from the user's creation, we can change this to something else.
			Calendar creationDate = new GregorianCalendar();
			creationDate.setTime(user.getAuditInfo().getCreationDate());
			int offset = 0;
			Calendar dateFrom;
			Calendar dateTo;
			long timeInMillis = date.getTime();
			do {
				dateFrom = (Calendar) creationDate.clone();
				dateFrom.add(BANDWIDTH_TIME_PERIOD_FIELD, offset);
				dateTo = (Calendar) dateFrom.clone();
				dateTo.add(BANDWIDTH_TIME_PERIOD_FIELD, 1);
				offset += BANDWIDTH_TIME_PERIOD_AMOUNT;
			} while (!(dateFrom.getTimeInMillis() <= timeInMillis &&
						dateTo.getTimeInMillis() > timeInMillis));

			ai = new AccountingInfo(user, dateFrom.getTime(), dateTo.getTime());
		}

		// Do the update.
		ai.updateBandwidth(bandwidthDiff);
		save(ai);
	}

}
