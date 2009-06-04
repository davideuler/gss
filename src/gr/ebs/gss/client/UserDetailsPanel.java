/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.rest.resource.UserResource;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * The panel that displays a status bar with quota information.
 */
public class UserDetailsPanel extends Composite {
	public static final boolean DONE = false;

	/**
	 * The label that displays user information.
	 */
	private HTML userInfoLabel;

	/**
	 * The constructor of the user details panel.
	 */
	public UserDetailsPanel() {
		final HorizontalPanel outer = new HorizontalPanel();
		outer.setSpacing(8);
		userInfoLabel = new HTML("&nbsp;");
		outer.add(userInfoLabel);
		outer.setCellHorizontalAlignment(userInfoLabel, HasHorizontalAlignment.ALIGN_RIGHT);
		outer.setStyleName("statusbar-inner");

		initWidget(outer);

		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return displayUserInfo();
			}
		});
	}

	/**
	 * Display the user information on the panel.
	 *
	 * @return true if the work has been carried out successfully
	 */
	protected boolean displayUserInfo() {
		UserResource user = GSS.get().getCurrentUserResource();
		if (user == null)
			return !DONE;
		userInfoLabel.setHTML("<b>" + user.getName() + ", " + user.getUsername() + "</b>");
		return DONE;
	}

}
