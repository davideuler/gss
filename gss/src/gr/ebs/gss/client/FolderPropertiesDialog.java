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

import gr.ebs.gss.client.FilePropertiesDialog.Images;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

	Set<PermissionDTO> permissions = null;

	private List<GroupDTO> groups = null;

	final PermissionsList permList;

	/**
	 * The widget that holds the folderName of the folder.
	 */
	private TextBox folderName = new TextBox();

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
	public FolderPropertiesDialog(final Images images, final boolean _create, Set<PermissionDTO> permissions, final List<GroupDTO> groups) {
		setAnimationEnabled(true);

		create = _create;
		final FolderDTO folder = (FolderDTO) GSS.get().getFolders().getCurrent().getUserObject();
		permList = new PermissionsList(images, permissions, folder.getOwner());
		this.permissions = permissions;
		this.groups = groups;
		// Use this opportunity to set the dialog's caption.
		if (create)
			setText("Create folder");
		else
			setText("Folder properties");

		// Outer contains inner and buttons
		final VerticalPanel outer = new VerticalPanel();
		// Inner contains generalPanel and permPanel
		final TabPanel inner = new TabPanel();
		final VerticalPanel generalPanel = new VerticalPanel();
		final VerticalPanel permPanel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		final HorizontalPanel permButtons = new HorizontalPanel();
		inner.add(generalPanel, "General");
		if (!create)
			inner.add(permPanel, "Permissions");
		inner.selectTab(0);

		final FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Parent");
		generalTable.setText(2, 0, "Creator");
		generalTable.setText(3, 0, "Date");
		folderName.setText(create ? "" : folder.getName());
		generalTable.setWidget(0, 1, folderName);
		if (create)
			generalTable.setText(1, 1, folder.getName());
		else
			generalTable.setText(1, 1, folder.getParent() == null ? "-" : folder.getParent().getName());
		generalTable.setText(2, 1, folder.getOwner().getName());
		final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		generalTable.setText(3, 1, formatter.format(folder.getAuditInfo().getCreationDate()));
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'Quit' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {

				createOrUpdateFolder(GSS.get().getCurrentUser().getId());

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

		final Button add = new Button("Add Group", new ClickListener() {

			public void onClick(Widget sender) {
				// hide();
				PermissionsAddDialog dlg = new PermissionsAddDialog(images, groups, permList, false);
				dlg.center();
			}
		});
		permButtons.add(add);
		permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

		final Button addUser = new Button("Add User", new ClickListener() {

			public void onClick(Widget sender) {
				// hide();
				PermissionsAddDialog dlg = new PermissionsAddDialog(images, groups, permList, true);
				dlg.center();
			}
		});
		permButtons.add(addUser);
		permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

		permButtons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		permButtons.setSpacing(8);
		permButtons.addStyleName("gwt-TabPanelBottom");

		generalPanel.add(generalTable);
		permPanel.add(permList);
		permPanel.add(permButtons);
		outer.add(inner);
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("gwt-TabPanelBottom");

		setWidget(outer);

	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#center()
	 */

	public void center() {
		super.center();
		folderName.setFocus(true);
	}

	/**
	 * Generate an RPC request to modify a folder.
	 *
	 * @param userId the ID of the current user
	 */
	private void modifyFolder(final Long userId) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		if (folder == null) {
			GSS.get().displayError("No folder was selected!");
			return;
		}
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		GWT.log("modifyFolder(" + userId + "," + folderId + ")", null);
		//update only if folder name is changed
		if (!((FolderDTO) folder.getUserObject()).getName().equals(folderName.getText()))
			service.modifyFolder(userId, folderId, folderName.getText(), new AsyncCallback() {

				public void onSuccess(final Object result) {
					GSS.get().getFolders().onFolderUpdate(folder);
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		else
			GWT.log("no changes in name", null);
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
				createOrUpdateFolder(GSS.get().getCurrentUser().getId());
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

	/**
	 * Generate an RPC request to create a new folder.
	 *
	 * @param userId the ID of the user whose namespace will be searched for
	 *            folders
	 * @param _folderName the name of the folder to create
	 */
	private void createFolder(final Long userId) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem selectedParentItem = GSS.get().getFolders().getCurrent();
		if (selectedParentItem == null) {
			GSS.get().displayError("No parent was selected!");
			return;
		}
		final FolderDTO selectedParent = (FolderDTO) selectedParentItem.getUserObject();
		final Long parentId = selectedParent.getId();
		final String _folderName = folderName.getText();
		if (_folderName == null || _folderName.length() == 0) {
			GSS.get().displayError("Empty folder name!");
			return;
		}
		GWT.log("createFolder(" + userId + "," + parentId + "," + _folderName + ")", null);
		service.createFolder(userId, parentId, _folderName, new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), selectedParentItem);
			}

			public void onFailure(final Throwable caught) {
				if (caught instanceof DuplicateNameException)
					GSS.get().displayError(caught.getMessage());
				else {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			}
		});
	}

	/**
	 * Upon closing the dialog by clicking OK or pressing ENTER this method does
	 * the actual work of modifying folder properties or creating a new Folder
	 * depending on the value of the create field
	 *
	 * @param userId
	 */
	private void createOrUpdateFolder(final Long userId) {
		if (create)
			createFolder(userId);
		else {
			permList.updatePermissionsAccordingToInput();
			if(permList.hasChanges())
				updatePermissions();
			else{
				GWT.log("no changes in permissions", null);
				modifyFolder(userId);
			}
		}
	}

	private void updatePermissions() {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.setFolderPermissions(GSS.get().getCurrentUser().getId(), ((FolderDTO) GSS.get().getCurrentSelection()).getId(), permList.permissions, new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getMySharesItem());
				modifyFolder(GSS.get().getCurrentUser().getId());
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
