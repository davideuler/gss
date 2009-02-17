/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client;

import gr.ebs.gss.client.domain.UserDTO;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author kman
 */
public class UserSuggestion implements Suggestion, Serializable {//extends MultiWordSuggestion {

	private UserDTO userDTO = null;

	public UserSuggestion(UserDTO user) {
		//super(user.getEmail(), user.getFirstname()+" "+user.getLastname());
		GWT.log(user.getEmail()+" "+ user.getFirstname()+" "+user.getLastname(),null);
		userDTO = user;
	}
	/**
     * Gets the display string associated with this suggestion. The
     * interpretation of the display string depends upon the value of
     * its oracle's {@link com.google.gwt.user.client.ui.SuggestOracle#isDisplayStringHTML()}.
     *
     * @return the display string for this suggestion
     */
    public String getDisplayString() {
        return userDTO.getUsername();
    }

    /**
     * Gets the replacement string associated with this suggestion. When
     * this suggestion is selected, the replacement string will be entered
     * into the SuggestBox's text box.
     *
     * @return the string to be entered into the SuggestBox's text box when
     *         this suggestion is selected
     */
    public String getReplacementString() {
    	return userDTO.getUsername();
    }
	/**
	 * @return the userDTO
	 */
	public UserDTO getUserDTO() {
		return userDTO;
	}

}
