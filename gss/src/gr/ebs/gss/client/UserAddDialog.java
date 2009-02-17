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

import gr.ebs.gss.client.Groups.Images;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author kman
 */
public class UserAddDialog extends DialogBox {

	ServerSuggestOracle suggestOracle = new ServerSuggestOracle();

	SuggestBox suggestBox = new SuggestBox(suggestOracle);
	UserDTO selectedUser=null;
	Label nameLabel;
	Label usernameLabel;


	final FlexTable userTable = new FlexTable();

	/**
	 * The widget's constructor.
	 *
	 * @param images the image icons from the file properties dialog
	 * @param _create true if the dialog is displayed for creating a new
	 *            sub-folder of the selected folder, false if it is displayed
	 *            for modifying the selected folder
	 */
	public UserAddDialog(final Images images) {
		setAnimationEnabled(true);
		setText("Add User");
		final VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		panel.addStyleName("gwt-TabPanelBottom");
		userTable.addStyleName("gss-permList");
		userTable.setWidget(0, 0, new Label("Enter Username:"));
		userTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
        suggestBox.setLimit(10);
        suggestBox.addEventHandler(new SuggestionHandler(){

			public void onSuggestionSelected(SuggestionEvent event) {
				UserSuggestion sug = (UserSuggestion) event.getSelectedSuggestion();
				selectedUser = sug.getUserDTO();
				usernameLabel.setText(selectedUser.getUsername());
				nameLabel.setText(selectedUser.getName());
				GWT.log(selectedUser+"", null);
			}

        });
        userTable.setWidget(0, 1, suggestBox);
        userTable.setWidget(1, 0, new Label("Selected User:"));
        userTable.getFlexCellFormatter().setStyleName(1, 0, "props-toplabels");
        userTable.setWidget(2, 0,new Label("Username:"));
        userTable.getFlexCellFormatter().setStyleName(2, 0, "props-toplabels");
        userTable.setWidget(2, 1,usernameLabel = new Label(""));
        userTable.setWidget(3, 0,new Label("Name:"));
        userTable.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        userTable.getFlexCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        userTable.getFlexCellFormatter().setStyleName(3, 0, "props-toplabels");
        nameLabel = new Label("");
        userTable.setWidget(3, 1, nameLabel);
        panel.add(userTable);
		final HorizontalPanel buttons = new HorizontalPanel();
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				addUser();
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickListener() {

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
		//suggestBox.setFocus(true);
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#center()
	 */
	public void center() {
		// TODO Auto-generated method stub
		super.center();
		suggestBox.setFocus(true);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.PopupPanel#onKeyDownPreview(char, int)
	 */
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				hide();
				//createGroup(GSS.get().getCurrentUser().getId(), groupName.getText());
				addUser();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

	/**
	 * Generate an RPC request to create a new group.
	 *
	 * @param userId the ID of the user whose namespace will be searched for
	 *            groups
	 * @param groupName the name of the group to create
	 */
	private void addUser() {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		GroupDTO group = (GroupDTO) GSS.get().getCurrentSelection();
		if ( group == null ) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		if ( selectedUser == null ) {
			GSS.get().displayError("No User Selected!");
			return;
		}
		service.addUserToGroup(GSS.get().getCurrentUser().getId(), group.getId(), selectedUser.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getGroups().updateGroups(GSS.get().getCurrentUser().getId());
				GSS.get().showUserList();
			}

			public void onFailure(final Throwable caught) {

				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());

			}
		});
	}
}
