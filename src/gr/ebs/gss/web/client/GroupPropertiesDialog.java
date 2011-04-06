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

import gr.ebs.gss.web.client.rest.PostCommand;
import gr.ebs.gss.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	 * @param _create true if the dialog is displayed for creating a new
	 *            sub-folder of the selected folder, false if it is displayed
	 *            for modifying the selected folder
	 */
	public GroupPropertiesDialog(final boolean _create) {
		setAnimationEnabled(true);
		create = _create;
		// Use this opportunity to set the dialog's caption.
		if (create)
			setText("Create Group");
		else
			setText("Group properties");
		final VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		groupName.getElement().setId("groupDialog.textBox.name");
		final Grid generalTable = new Grid(1, 2);
		generalTable.setText(0, 0, "Group Name");
		generalTable.setWidget(0, 1, groupName);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);
		final HorizontalPanel buttons = new HorizontalPanel();
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createGroup(groupName.getText());
				hide();
			}
		});
		ok.getElement().setId("groupDialog.button.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		cancel.getElement().setId("groupDialog.button.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-TabPanelBottom");
		panel.add(buttons);
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		//panel.addStyleName("gss-DialogBox");
		panel.addStyleName("gss-TabPanelBottom");		
	}
	@Override
	public void center() {
		super.center();
		groupName.setFocus(true);
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
					hide();
					createGroup( groupName.getText());
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}


	/**
	 * Generate an RPC request to create a new group.
	 *
	 * @param userId the ID of the user whose namespace will be searched for
	 *            groups
	 * @param aGroupName the name of the group to create
	 */
	private void createGroup(String aGroupName) {

		if (aGroupName == null || aGroupName.length() == 0) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		GWT.log("createGroup(" + aGroupName + ")", null);
		PostCommand cg = new PostCommand(GSS.get().getCurrentUserResource().getGroupsPath()+"?name="+URL.encodeComponent(aGroupName), "", 201){

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
						GSS.get().displayError("Resource does not exist");
					else if(statusCode == 409)
						GSS.get().displayError("A group with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to create group:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error creating group:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(cg);

	}
}
