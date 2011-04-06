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
package gr.ebs.gss.web.client;

import gr.ebs.gss.web.client.rest.GetCommand;
import gr.ebs.gss.web.client.rest.PostCommand;
import gr.ebs.gss.web.client.rest.RestException;
import gr.ebs.gss.web.client.rest.resource.GroupResource;
import gr.ebs.gss.web.client.rest.resource.UserResource;
import gr.ebs.gss.web.client.rest.resource.UserSearchResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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
		panel.addStyleName("gss-TabPanelBottom");
		userTable.addStyleName("gss-permList");
		userTable.setWidget(0, 0, new Label("Username:"));
		userTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		suggestBox.getTextBox().addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				if (selectedUser != null && selectedUser.endsWith("@"))
					updateSuggestions();
			}
		});

		suggestBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				// Ignore the arrow keys.
				int keyCode=event.getNativeKeyCode();
				if(keyCode == KeyCodes.KEY_UP ||
						keyCode == KeyCodes.KEY_DOWN ||
						keyCode == KeyCodes.KEY_LEFT ||
						keyCode == KeyCodes.KEY_RIGHT)
					return;
				if (keyCode==KeyCodes.KEY_ESCAPE) {
					suggestBox.hideSuggestionList();
					return;
				}
				String text = suggestBox.getText().trim();
				// Avoid useless queries for keystrokes that do not modify the
				// text.
				if (text.equals(selectedUser))
					return;
				selectedUser = text;
				// Go to the server only if the user typed the @ character.
				if (selectedUser.endsWith("@"))
					updateSuggestions();
			}
		});
		suggestBox.getElement().setId("addUser.textBox");
        userTable.setWidget(0, 1, suggestBox);
        panel.add(userTable);
		HorizontalPanel buttons = new HorizontalPanel();
		Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addUser();
				hide();
			}
		});
		ok.getElement().setId("addUser.button.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		cancel.getElement().setId("addUser.button.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-TabPanelBottom");
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
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					addUser();
					hide();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
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
		PostCommand cg = new PostCommand(group.getUri()+"?name="+selectedUser, "", 201){
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

		GetCommand<UserSearchResource> eg = new GetCommand<UserSearchResource>(UserSearchResource.class,
					app.getApiPath() + "users/" + URL.encodeComponent(query), false, null) {

			@Override
			public void onComplete() {
				suggestBox.hideSuggestionList();
				oracle.clear();
				UserSearchResource s = getResult();
				for (UserResource user : s.getUsers()) {
					GWT.log("Found " + user.getUsername(), null);
					oracle.add(user.getUsername());
				}
				suggestBox.showSuggestionList();
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
