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
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.PermissionHolder;

import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

	private List<GroupResource> groups = null;

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

	final FolderResource folder;

	final TabPanel inner;

	/**
	 * The widget's constructor.
	 *
	 * @param images the image icons from the file properties dialog
	 * @param _create true if the dialog is displayed for creating a new
	 *            sub-folder of the selected folder, false if it is displayed
	 *            for modifying the selected folder
	 */
	public FolderPropertiesDialog(Images images, boolean _create,  final List<GroupResource> _groups) {
		setAnimationEnabled(true);

		// Enable IE selection for the dialog (must disable it upon closing it)
		GSS.enableIESelection();

		create = _create;
		DnDTreeItem folderItem = (DnDTreeItem)GSS.get().getFolders().getCurrent();
		folder = folderItem.getFolderResource();
		permList = new PermissionsList(images, folder.getPermissions(), folder.getOwner());
		groups = _groups;
		// Use this opportunity to set the dialog's caption.
		if (create)
			setText("Create folder");
		else
			setText("Folder properties");

		// Outer contains inner and buttons
		VerticalPanel outer = new VerticalPanel();
		// Inner contains generalPanel and permPanel
		inner = new TabPanel();
		VerticalPanel generalPanel = new VerticalPanel();
		VerticalPanel permPanel = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();
		HorizontalPanel permButtons = new HorizontalPanel();
		inner.add(generalPanel, "General");
		if (!create)
			inner.add(permPanel, "Sharing");
		inner.selectTab(0);

		FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Parent");
		generalTable.setText(2, 0, "Creator");
		generalTable.setText(3, 0, "Date");
		folderName.setText(create ? "" : folder.getName());
		generalTable.setWidget(0, 1, folderName);
		if (create)
			generalTable.setText(1, 1, folder.getName());
		else if(folder.getParentName() == null)
			generalTable.setText(1, 1, "-");
		else
			generalTable.setText(1, 1, folder.getParentName());
		generalTable.setText(2, 1, folder.getOwner());
		DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		if(folder.getCreationDate() != null)
			generalTable.setText(3, 1, formatter.format(folder.getCreationDate()));
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
		Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {

				createOrUpdateFolder();

				closeDialog();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		Button cancel = new Button("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				closeDialog();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gwt-TabPanelBottom");

		Button add = new Button("Add Group", new ClickListener() {

			public void onClick(Widget sender) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, false);
				dlg.center();
			}
		});
		permButtons.add(add);
		permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

		Button addUser = new Button("Add User", new ClickListener() {

			public void onClick(Widget sender) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, true);
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

		/*if (create)
			folderName.setFocus(true);
		else
			ok.setFocus(true);*/
	}

	@Override
	public void center() {
		super.center();
		folderName.setFocus(true);
	}

	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				closeDialog();
				createOrUpdateFolder();
				break;
			case KeyboardListener.KEY_ESCAPE:
				closeDialog();
				break;
		}

		return true;
	}

	/**
	 * Enables IE selection prevention and hides the dialog
	 * (we disable the prevention on creation of the dialog)
	 */
	public void closeDialog() {
		GSS.preventIESelection();
		hide();
	}

	/**
	 * Generate an RPC request to create a new folder.
	 *
	 * @param userId the ID of the user whose namespace will be searched for
	 *            folders
	 * @param _folderName the name of the folder to create
	 */
	private void createFolder() {
		ExecutePost ep = new ExecutePost(folder.getUri()+"?new="+folderName.getText(),"", 201){

			@Override
			public void onComplete() {
				GSS.get().getFolders().updateFolder( (DnDTreeItem) GSS.get().getFolders().getCurrent());
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions or a folder with same name already exists");
					else if(statusCode == 404)
						GSS.get().displayError("Resource not found");
					else
						GSS.get().displayError("Unable to create folder:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error creating folder:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(ep);

	}

	/**
	 * Upon closing the dialog by clicking OK or pressing ENTER this method does
	 * the actual work of modifying folder properties or creating a new Folder
	 * depending on the value of the create field
	 *
	 * @param userId
	 */
	private void createOrUpdateFolder() {
		if (create)
			createFolder();
		else
			updateFolder();

	}

	private void updateFolder() {
		permList.updatePermissionsAccordingToInput();
		Set<PermissionHolder> perms = permList.getPermissions();
		JSONObject json = new JSONObject();
		if(!folder.getName().equals(folderName.getText()))
			json.put("name", new JSONString(folderName.getText()));
		JSONArray perma = new JSONArray();
		int i=0;
		for(PermissionHolder p : perms){
			JSONObject po = new JSONObject();
			if(p.getUser() != null)
				po.put("user", new JSONString(p.getUser()));
			if(p.getGroup() != null)
				po.put("group", new JSONString(p.getGroup()));
			po.put("read", JSONBoolean.getInstance(p.isRead()));
			po.put("write", JSONBoolean.getInstance(p.isWrite()));
			po.put("modifyACL", JSONBoolean.getInstance(p.isModifyACL()));
			perma.set(i,po);
			i++;
		}
		json.put("permissions", perma);
		GWT.log(json.toString(), null);
		ExecutePost ep = new ExecutePost(folder.getUri()+"?update=", json.toString(), 200){

			@Override
			public void onComplete() {
				if(getPostBody() != null && !"".equals(getPostBody().trim())){
					DnDTreeItem folderItem = (DnDTreeItem) GSS.get().getFolders().getCurrent();
					FolderResource fres = folderItem.getFolderResource();
					String initialPath = fres.getUri();
					String newPath =  getPostBody().trim();
					fres.setUri(newPath);

					if(folderItem.getParentItem() != null && ((DnDTreeItem)folderItem.getParentItem()).getFolderResource() != null){
						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().removeSubfolderPath(initialPath);
						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().getSubfolderPaths().add(newPath);
					}
				}
				GSS.get().getFolders().updateFolder( (DnDTreeItem) GSS.get().getFolders().getCurrent());
				GSS.get().showFileList(true);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions or" +
								" a folder with same name already exists");
					else if(statusCode == 404)
						GSS.get().displayError("Resource not found, or user specified in sharing does not exist");
					else
						GSS.get().displayError("Unable to update folder: "+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error moifying file: "+t.getMessage());
			}
		};
		DeferredCommand.addCommand(ep);
	}

	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}

}
