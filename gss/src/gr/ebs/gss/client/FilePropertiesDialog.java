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

import gr.ebs.gss.client.domain.FileBodyDTO;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File properties' dialog box implementation.
 */
/**
 * @author past
 */
public class FilePropertiesDialog extends DialogBox {

	final PermissionsList permList;

	private CheckBox readForAll;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ImageBundle {
		@Resource("gr/ebs/gss/resources/edit_user.png")
		AbstractImagePrototype permUser();

		@Resource("gr/ebs/gss/resources/groupevent.png")
		AbstractImagePrototype permGroup();

		@Resource("gr/ebs/gss/resources/editdelete.png")
		AbstractImagePrototype delete();

		@Resource("gr/ebs/gss/resources/db_update.png")
		AbstractImagePrototype restore();

		@Resource("gr/ebs/gss/resources/download_manager.png")
		AbstractImagePrototype download();
	}

	/**
	 * The widget that holds the name of the file.
	 */
	private TextBox name = new TextBox();

	/**
	 * Text box with the tags associated with the file
	 */
	private TextBox tags = new TextBox();

	/**
	 * A FlowPanel with all user tags
	 */
	private FlowPanel allTagsContent;

	private String initialTags;

	private final CheckBox versioned = new CheckBox();
	/**
	 * The widget's constructor.
	 *
	 * @param images the dialog's ImageBundle
	 * @param permissions
	 * @param groups
	 * @param bodies
	 */
	public FilePropertiesDialog(final Images images, Set<PermissionDTO> permissions, final List<GroupDTO> groups,  List<FileBodyDTO> bodies) {
		// Use this opportunity to set the dialog's caption.
		setText("File properties");
		setAnimationEnabled(true);
		final FileHeaderDTO file = (FileHeaderDTO) GSS.get().getCurrentSelection();
		permList = new PermissionsList(images, permissions, file.getOwner());

		// Outer contains inner and buttons
		final VerticalPanel outer = new VerticalPanel();
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel
		final TabPanel inner = new TabPanel();
		final VerticalPanel generalPanel = new VerticalPanel();
		final VerticalPanel permPanel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		final HorizontalPanel permButtons = new HorizontalPanel();
		final HorizontalPanel permForAll = new HorizontalPanel();
		final VerticalPanel verPanel = new VerticalPanel();
		final HorizontalPanel vPanel = new HorizontalPanel();
		final HorizontalPanel vPanel2 = new HorizontalPanel();

		versioned.setChecked(file.isVersioned());
		inner.add(generalPanel, "General");
		inner.add(permPanel, "Permissions");
		inner.add(verPanel, "Versions");
		inner.selectTab(0);

		final FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Folder");
		generalTable.setText(2, 0, "Owner");
		generalTable.setText(3, 0, "Date");
		generalTable.setText(4, 0, "Tags");
		generalTable.setText(5, 0, "URI");
		name.setText(file.getName());
		generalTable.setWidget(0, 1, name);
		if (GSS.get().getFolders().getCurrent() != null && GSS.get().getFolders().getCurrent().getUserObject() instanceof FolderDTO) {
			FolderDTO folder = (FolderDTO) GSS.get().getFolders().getCurrent().getUserObject();
			generalTable.setText(1, 1, folder.getName());
		} else if (GSS.get().getFolders().getCurrent() != null && GSS.get().getFolders().getCurrent().getUserObject() instanceof UserDTO) {
			UserDTO folder = (UserDTO) GSS.get().getFolders().getCurrent().getUserObject();
			generalTable.setText(1, 1, folder.getName());
		}
		generalTable.setText(2, 1, file.getOwner().getName());
		final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		generalTable.setText(3, 1, formatter.format(file.getAuditInfo().getCreationDate()));
		// Get the tags
		StringBuffer tagsBuffer = new StringBuffer();
		Iterator i = file.getTags().iterator();
		while (i.hasNext()) {
			String tag = (String) i.next();
			tagsBuffer.append(tag).append(", ");
		}
		if (tagsBuffer.length() > 1)
			tagsBuffer.delete(tagsBuffer.length() - 2, tagsBuffer.length() - 1);
		tags.setText(tagsBuffer.toString());
		initialTags = tags.getText();
		generalTable.setWidget(4, 1, tags);
		TextBox path = new TextBox();
		path.setText(file.getURI());
		path.setTitle("Use this URI for sharing this file with the world");
		path.setReadOnly(true);
		generalTable.setWidget(5, 1, path);
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(4, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(5, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(4, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(5, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				accept(GSS.get().getCurrentUser().getId());
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

		generalPanel.add(generalTable);

		// Asynchronously retrieve the tags defined by this user
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				updateTags(GSS.get().getCurrentUser().getId());
			}
		});

		DisclosurePanel allTags = new DisclosurePanel("All tags");
		allTagsContent = new FlowPanel();
		allTags.setContent(allTagsContent);
		generalPanel.add(allTags);
		generalPanel.setSpacing(4);

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

		readForAll = new CheckBox();
		readForAll.setChecked(file.isReadForAll());
		permForAll.add(new Label("Read For All:"));
		permForAll.add(readForAll);
		permForAll.setSpacing(8);
		permForAll.addStyleName("gwt-TabPanelBottom");
		permPanel.add(permList);
		permPanel.add(permButtons);
		permPanel.add(permForAll);

		VersionsList verList = new VersionsList(this, images, bodies);
		verPanel.add(verList);

		vPanel.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.setSpacing(8);
		vPanel.addStyleName("gwt-TabPanelBottom");
		vPanel.add(new Label("Versioned:"));

		vPanel.add(versioned);
		verPanel.add(vPanel);
		vPanel2.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		vPanel2.setSpacing(8);
		vPanel2.addStyleName("gwt-TabPanelBottom");
		Button removeVersionsButton = new Button(images.delete().getHTML(),new ClickListener(){

			public void onClick(Widget sender) {
				//TODO: replace javascript confirmation dialog
				boolean confirm = Window.confirm("Really remove all previous versions?");
				if(confirm){
					hide();
					removeAllOldVersions(GSS.get().getCurrentUser().getId(), file.getId());
				}

			}



		});
		HTML removeAllVersion = new HTML("<span>Remove all previous versions?</span>");
		vPanel2.add(removeAllVersion);
		vPanel2.add(removeVersionsButton);
		verPanel.add(vPanel2);
		outer.add(inner);
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("gwt-TabPanelBottom");

		focusPanel.setFocus(true);
		setWidget(outer);
	}

	/**
	 * Retrieves all user tags from the server and updates the FlowPanel
	 *
	 * @param userId
	 */
	private void updateTags(Long userId) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getUserTags(userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				Set userTags = (Set) result;
				allTagsContent.clear();

				Iterator t = userTags.iterator();
				while (t.hasNext()) {
					final Button tag = new Button((String) t.next(), new ClickListener() {

						public void onClick(Widget sender) {
							String existing = tags.getText();
							String newTag = ((Button) sender).getText().trim();
							// insert the new tag only if it is not in the list
							// already
							if (existing.indexOf(newTag + ",") == -1 && !existing.trim().endsWith(newTag))
								tags.setText(existing.trim() + (existing.length() > 0 ? ", " : "") + newTag);
						}
					});
					allTagsContent.add(tag);
				}
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

	@Override
	@SuppressWarnings("fallthrough")
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				accept(GSS.get().getCurrentUser().getId());
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

	/**
	 * Accepts any change and updates the file
	 *
	 * @param userId
	 */
	private void accept(final Long userId) {

		final GSSServiceAsync service = GSS.get().getRemoteService();
		final FileHeaderDTO file = (FileHeaderDTO) GSS.get().getCurrentSelection();
		if(file.isVersioned() != versioned.isChecked())
			toggleVersioned(GSS.get().getCurrentUser().getId(), file.getId(), versioned.isChecked());
		if (name.getText().equals(file.getName()) && tags.getText().equals(initialTags)) {
			GWT.log("no changes in name or tags", null);
			updatePermissions();
			return;
		}
		service.updateFile(userId, file.getId(), name.getText(), tags.getText(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				updatePermissions();
				GSS.get().getFileList().updateFileCache(userId);
				GWT.log("File " + file.getId() + " update successful", null);
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

	private void updatePermissions() {
		permList.updatePermissionsAccordingToInput();
		final FileHeaderDTO file = (FileHeaderDTO) GSS.get().getCurrentSelection();
		if (!permList.hasChanges() && readForAll.isChecked() == file.isReadForAll()) {
			GWT.log("no changes in permissions", null);
			return;
		}
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.setFilePermissions(GSS.get().getCurrentUser().getId(), ((FileHeaderDTO) GSS.get().getCurrentSelection()).getId(), readForAll.isChecked(), permList.permissions, new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
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

	private void removeAllOldVersions(Long userId, Long fileId) {
		GSS.get().getRemoteService().removeOldVersions(userId, fileId, new AsyncCallback(){

			public void onFailure(Throwable caught) {
				// GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());

			}

			public void onSuccess(Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
			}

		});
	}

	private void toggleVersioned(Long userId, Long fileId, boolean version){
		GSS.get().getRemoteService().toggleFileVersioning(userId, fileId, version, new AsyncCallback(){

			public void onFailure(Throwable caught) {
				// GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}

			public void onSuccess(Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
			}

		});
	}
}
