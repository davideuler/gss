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

import gr.ebs.gss.client.GroupMenu.Images;
import gr.ebs.gss.client.exceptions.RpcException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author kman
 */
public class GroupPropertiesDialog extends DialogBox {

	/**
	 * The widget that holds the folderName of the folder.
	 */
	private TextBox groupName = new TextBox();

	/**
	 * A flag that denotes whether the dialog will be used to create or modify a
	 * folder.
	 */
	private final boolean create;

	/**
	 * The widget's constructor.
	 *
	 * @param images the image icons from the file properties dialog
	 * @param _create true if the dialog is displayed for creating a new
	 *            sub-folder of the selected folder, false if it is displayed
	 *            for modifying the selected folder
	 */
	public GroupPropertiesDialog(final Images images, final boolean _create) {
		setAnimationEnabled(true);
		create = _create;
		// Use this opportunity to set the dialog's caption.
		if (create)
			setText("Create Group");
		else
			setText("Group properties");
		final VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		final Grid generalTable = new Grid(1, 2);
		generalTable.setText(0, 0, "Group Name");
		generalTable.setWidget(0, 1, groupName);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);
		final HorizontalPanel buttons = new HorizontalPanel();
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				createGroup(GSS.get().getCurrentUser().getId(),groupName.getText());
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
		//panel.addStyleName("gss-DialogBox");
		panel.addStyleName("gwt-TabPanelBottom");
		groupName.setFocus(true);
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
				createGroup(GSS.get().getCurrentUser().getId(), groupName.getText());
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
	private void createGroup(final Long userId, final String groupName) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		if (groupName == null || groupName.length() == 0) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		GWT.log("createGroup(" + userId + "," + groupName + ")", null);
		service.createGroup(userId, groupName, new AsyncCallback() {

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
