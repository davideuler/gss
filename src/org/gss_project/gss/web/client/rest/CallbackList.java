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
package org.gss_project.gss.web.client.rest;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * @author kman
 *
 */
public class CallbackList<T> implements AsyncCallback<T>{
	List<T> result = new ArrayList();
	boolean erroneous = false;

	@Override
	public void onFailure(Throwable arg0) {
		GWT.log("Error in callback list", arg0);
		erroneous = true;

	}


	@Override
	public void onSuccess(T arg0) {
		result.add(arg0);
	}



	/**
	 * Retrieve the result.
	 *
	 * @return the result
	 */
	public List<T> getResult() {
		return result;
	}



	/**
	 * Retrieve the erroneous.
	 *
	 * @return the erroneous
	 */
	public boolean isErroneous() {
		return erroneous;
	}





}
