/*
 * Copyright 2007, 2008, 2009, 2010 Electronic Business Systems Ltd.
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
import gr.ebs.gss.client.rest.PostCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.PermissionHolder;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;

import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

	private List<GroupResource> groups = null;

	final PermissionsList permList;

	private CheckBox readForAll;

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
		
		folder = ((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource();
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
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);
		VerticalPanel generalPanel = new VerticalPanel();
		VerticalPanel permPanel = new VerticalPanel();
		final HorizontalPanel permForAll = new HorizontalPanel();
		final HorizontalPanel pathPanel = new HorizontalPanel();
		HorizontalPanel buttons = new HorizontalPanel();
		HorizontalPanel permButtons = new HorizontalPanel();

		inner.add(generalPanel, "General");
		if (!create)
			inner.add(permPanel, "Sharing");
		inner.selectTab(0);

		final Label folderNameNote = new Label("Please note that slashes ('/') are not allowed in folder names.", true);
		folderNameNote.setVisible(false);
		folderNameNote.setStylePrimaryName("gss-readForAllNote");

		FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Parent");
		generalTable.setText(2, 0, "Creator");
		generalTable.setText(3, 0, "Last modified");
		folderName.setText(create ? "" : folder.getName());
		folderName.getElement().setId("folderPropertiesDialog.textBox.name");
		generalTable.setWidget(0, 1, folderName);
		folderName.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				if(folderName.getText().contains("/"))
					folderNameNote.setVisible(true);
				else
					folderNameNote.setVisible(false);				
				
			}
		});


		if (create)
			generalTable.setText(1, 1, folder.getName());
		else if(folder.getParentName() == null)
			generalTable.setText(1, 1, "-");
		else
			generalTable.setText(1, 1, folder.getParentName());
		generalTable.setWidget(0, 2, folderNameNote);
		generalTable.setText(2, 1, folder.getOwner());
		DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		if(folder.getModificationDate() != null)
			generalTable.setText(3, 1, formatter.format(folder.getModificationDate()));
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'Create/Update' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		String okLabel;
		if (create)
			okLabel = "Create";
		else
			okLabel = "Update";
		Button ok = new Button(okLabel, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				if(folderName.getText().contains("/"))
					folderNameNote.setVisible(true);
				else {
					folderNameNote.setVisible(false);
					createOrUpdateFolder();
					closeDialog();
				}

			}
		});
		ok.getElement().setId("folderPropertiesDialog.button.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		cancel.getElement().setId("folderPropertiesDialog.button.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-TabPanelBottom");

		Button add = new Button("Add Group", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, false);
				dlg.center();
			}
		});
		permButtons.add(add);
		permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

		Button addUser = new Button("Add User", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, true);
				dlg.center();
			}
		});
		permButtons.add(addUser);
		permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

		permButtons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		permButtons.setSpacing(8);
		permButtons.addStyleName("gss-TabPanelBottom");

		final Label readForAllNote = new Label("When this option is enabled, the folder will be readable" +
					" by everyone. By checking this option, you are certifying that you have the right to " +
					"distribute this folder's contents and that it does not violate the Terms of Use.", true);
		readForAllNote.setVisible(false);
		readForAllNote.setStylePrimaryName("gss-readForAllNote");

		readForAll = new CheckBox();
		readForAll.setValue(folder.isReadForAll());
		readForAll.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				readForAllNote.setVisible(readForAll.getValue());
			}

		});

		generalPanel.add(generalTable);
		permPanel.add(permList);
		permPanel.add(permButtons);

		// Only show the read for all permission if the user is the owner.
		if (folder.getOwner().equals(GSS.get().getCurrentUserResource().getUsername())) {
			permForAll.add(new Label("Public"));
			permForAll.add(readForAll);
			permForAll.setSpacing(8);
			permForAll.addStyleName("gss-TabPanelBottom");
			permForAll.add(readForAllNote);
			permPanel.add(permForAll);
		}
		TextBox path = new TextBox();
		path.setWidth("100%");
		path.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GSS.enableIESelection();
				((TextBox) event.getSource()).selectAll();
				GSS.preventIESelection();
			}

		});
		path.setText(folder.getUri());
		path.setTitle("Use this link for sharing the folder via e-mail, IM, etc. (crtl-C/cmd-C to copy to system clipboard)");
		path.setWidth("100%");
		path.setReadOnly(true);
		pathPanel.setWidth("100%");
		pathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		pathPanel.add(new Label("Link"));
		pathPanel.setSpacing(8);
		pathPanel.addStyleName("gss-TabPanelBottom");
		pathPanel.add(path);
		permPanel.add(pathPanel);

		outer.add(inner);
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("gss-TabPanelBottom");

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
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					closeDialog();
					createOrUpdateFolder();
					break;
				case KeyCodes.KEY_ESCAPE:
					closeDialog();
					break;
			}
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
		String name = folderName.getText();
		if (!GSS.isValidResourceName(name)) {
			GSS.get().displayError("The folder name '" + name + "' is invalid");
			return;
		}
		PostCommand ep = new PostCommand(folder.getUri() + "?new=" +
				URL.encodeComponent(name), "", 201) {

			@Override
			public void onComplete() {
				//TODO:CELLTREE
				GSS.get().getTreeView().updateNodeChildren((RestResourceWrapper) GSS.get().getTreeView().getSelection());
				//GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getCurrent());
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary" +
								" permissions or a folder with same name " +
								"already exists");
					else if(statusCode == 404)
						GSS.get().displayError("Resource not found");
					else
						GSS.get().displayError("Unable to create folder:" +
								((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error creating folder:" +
							t.getMessage());
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
		//only update the read for all perm if the user is the owner
		if (readForAll.getValue() != folder.isReadForAll())
			if (folder.getOwner().equals(GSS.get().getCurrentUserResource().getUsername()))
				json.put("readForAll", JSONBoolean.getInstance(readForAll.getValue()));
		if (permList.hasChanges()) {
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
		}
		PostCommand ep = new PostCommand(folder.getUri()+"?update=", json.toString(), 200){

			@Override
			public void onComplete() {
				//TODO:CELLTREE
				
				if(getPostBody() != null && !"".equals(getPostBody().trim())){
					
					
					FolderResource fres = ((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource();
					String initialPath = fres.getUri();
					String newPath =  getPostBody().trim();
					fres.setUri(newPath);
					((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource().setUri(newPath);
					((RestResourceWrapper) GSS.get().getTreeView().getSelection()).setUri(newPath);
					GSS.get().getTreeView().updateNodeChildren(fres.getParentURI());
					/*
					if(folderItem.getParentItem() != null && ((DnDTreeItem)folderItem.getParentItem()).getFolderResource() != null){
						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().removeSubfolderPath(initialPath);
						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().getSubfolderPaths().add(newPath);
					}*/
				}
				//GSS.get().getFolders().updateFolder( (DnDTreeItem) GSS.get().getFolders().getCurrent());
				
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
				//TODO:CELLTREE
				//GSS.get().getFolders().updateFolder( (DnDTreeItem) GSS.get().getFolders().getCurrent());
			}
		};
		DeferredCommand.addCommand(ep);
	}

	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}

}
