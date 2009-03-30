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

import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author kman
 */
public class UserAddDialog extends DialogBox {

	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SuggestBox suggestBox = new SuggestBox(oracle);

	String selectedUser=null;
	FlexTable userTable = new FlexTable();

	/**
	 * The widget's constructor.
	 */
	public UserAddDialog() {
		setAnimationEnabled(true);
		setText("Add User");
		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		panel.addStyleName("gwt-TabPanelBottom");
		userTable.addStyleName("gss-permList");
		userTable.setWidget(0, 0, new Label("Username:"));
		userTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		suggestBox.addFocusListener(new FocusListenerAdapter() {
			@Override
			public void onFocus(Widget sender) {
				if (selectedUser != null && selectedUser.endsWith("@"))
					updateSuggestions();
			}
		});
		suggestBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				// Ignore the arrow keys.
				if (keyCode==KEY_UP || keyCode==KEY_DOWN || keyCode==KEY_LEFT || keyCode==KEY_RIGHT)
					return;
				String text = suggestBox.getText().trim();
				// Avoid useless queries for keystrokes that do not modify the text.
				if (text.equals(selectedUser))
					return;
				selectedUser = text;
				// Go to the server only if the user typed the @ character.
				if (selectedUser.endsWith("@"))
					updateSuggestions();
			}
		});
        userTable.setWidget(0, 1, suggestBox);
        panel.add(userTable);
		HorizontalPanel buttons = new HorizontalPanel();
		Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				addUser();
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gwt-TabPanelBottom");
		panel.add(buttons);
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		panel.addStyleName("gss-DialogBox");
	}

	@Override
	public void center() {
		super.center();
		suggestBox.setFocus(true);
	}

	@Override
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				hide();
				addUser();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}
		return true;
	}

	/**
	 * Generate a request to add a user to a group.
	 *
	 * @param groupName the name of the group to create
	 */
	private void addUser() {
		GroupResource group = (GroupResource) GSS.get().getCurrentSelection();
		selectedUser = suggestBox.getText();
		if ( group == null ) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		if ( selectedUser == null ) {
			GSS.get().displayError("No User Selected!");
			return;
		}
		ExecutePost cg = new ExecutePost(group.getPath()+"?name="+selectedUser, "", 201){
			@Override
			public void onComplete() {
				GSS.get().getGroups().updateGroups();
				GSS.get().showUserList();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("User does not exist");
					else if(statusCode == 409)
						GSS.get().displayError("A user with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to add user: "+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error adding user: "+t.getMessage());
			}
		};
		DeferredCommand.addCommand(cg);
	}

	/**
	 * Update the list of suggestions.
	 */
	protected void updateSuggestions() {
		final GSS app = GSS.get();
		String query = selectedUser.substring(0, selectedUser.length()-1);
		GWT.log("Searching for " + query, null);

		ExecuteGet<UserSearchResource> eg = new ExecuteGet<UserSearchResource>(UserSearchResource.class,
					GSS.GSS_REST_PATH+"users/"+URL.encodeComponent(query)){

			@Override
			public void onComplete() {
				DisplayHelper.hideSuggestions(suggestBox);
				oracle.clear();
				UserSearchResource s = getResult();
				for (UserResource user : s.getUsers()) {
					GWT.log("Found " + user.getUsername(), null);
					oracle.add(user.getUsername());
				}
				DisplayHelper.showSuggestions(suggestBox, selectedUser);
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException)
					app.displayError("Unable to perform search: "+((RestException)t).getHttpStatusText());
				else
					app.displayError("System error while searching for users: "+t.getMessage());
				GWT.log("", t);
				DisplayHelper.log(t.getMessage());
			}

		};
		DeferredCommand.addCommand(eg);
	}

}
