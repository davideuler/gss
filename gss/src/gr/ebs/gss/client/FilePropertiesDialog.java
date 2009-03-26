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

import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;
import gr.ebs.gss.client.rest.resource.PermissionHolder;
import gr.ebs.gss.client.rest.resource.TagsResource;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File properties' dialog box implementation.
 *
 * @author past
 */
public class FilePropertiesDialog extends DialogBox {

	final PermissionsList permList;

	private CheckBox readForAll;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends MessagePanel.Images {

		@Resource("gr/ebs/gss/resources/edit_user.png")
		AbstractImagePrototype permUser();

		@Resource("gr/ebs/gss/resources/groupevent.png")
		AbstractImagePrototype permGroup();

		@Resource("gr/ebs/gss/resources/editdelete.png")
		AbstractImagePrototype delete();

		@Resource("gr/ebs/gss/resources/db_update.png")
		AbstractImagePrototype restore();

		@Resource("gr/ebs/gss/resources/folder_inbox.png")
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

	private final CheckBox versioned = new CheckBox();

	final FileResource file;

	String initialTagText;

	final TabPanel inner;

	/**
	 * The widget's constructor.
	 *
	 * @param images the dialog's ImageBundle
	 * @param groups
	 * @param bodies
	 */
	public FilePropertiesDialog(final Images images, final List<GroupResource> groups, List<FileResource> bodies) {
		// Set the dialog's caption.
		setText("File properties");
		setAnimationEnabled(true);
		file = (FileResource) GSS.get().getCurrentSelection();
		permList = new PermissionsList(images, file.getPermissions(), file.getOwner());

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new TabPanel();
		final VerticalPanel generalPanel = new VerticalPanel();
		final VerticalPanel permPanel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		final HorizontalPanel permButtons = new HorizontalPanel();
		final HorizontalPanel permForAll = new HorizontalPanel();
		final HorizontalPanel pathPanel = new HorizontalPanel();
		final VerticalPanel verPanel = new VerticalPanel();
		final HorizontalPanel vPanel = new HorizontalPanel();
		final HorizontalPanel vPanel2 = new HorizontalPanel();

		versioned.setChecked(file.isVersioned());
		inner.add(generalPanel, "General");
		inner.add(permPanel, "Sharing");
		inner.add(verPanel, "Versions");
		inner.selectTab(0);

		final FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Folder");
		generalTable.setText(2, 0, "Owner");
		generalTable.setText(3, 0, "Date");
		generalTable.setText(4, 0, "Tags");
		name.setText(file.getName());
		generalTable.setWidget(0, 1, name);
		if (GSS.get().getFolders().getCurrent() != null && GSS.get().getFolders().getCurrent().getUserObject() instanceof FolderResource) {
			FolderResource folder = (FolderResource) GSS.get().getFolders().getCurrent().getUserObject();
			generalTable.setText(1, 1, folder.getName());
		} else if (GSS.get().getFolders().getCurrent() != null && GSS.get().getFolders().getCurrent().getUserObject() instanceof GroupUserResource) {
			GroupUserResource folder = (GroupUserResource) GSS.get().getFolders().getCurrent().getUserObject();
			generalTable.setText(1, 1, folder.getName());
		}
		generalTable.setText(2, 1, file.getOwner());
		final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		generalTable.setText(3, 1, formatter.format(file.getCreationDate()));
		// Get the tags.
		StringBuffer tagsBuffer = new StringBuffer();
		Iterator i = file.getTags().iterator();
		while (i.hasNext()) {
			String tag = (String) i.next();
			tagsBuffer.append(tag).append(", ");
		}
		if (tagsBuffer.length() > 1)
			tagsBuffer.delete(tagsBuffer.length() - 2, tagsBuffer.length() - 1);
		initialTagText = tagsBuffer.toString();
		tags.setText(initialTagText);
		generalTable.setWidget(4, 1, tags);
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(4, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(4, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				accept();
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
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

		// Asynchronously retrieve the tags defined by this user.
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				updateTags();
			}
		});

		DisclosurePanel allTags = new DisclosurePanel("All tags");
		allTagsContent = new FlowPanel();
		allTags.setContent(allTagsContent);
		generalPanel.add(allTags);
		generalPanel.setSpacing(4);

		final Button add = new Button("Add Group", new ClickListener() {

			public void onClick(Widget sender) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(images, groups, permList, false);
				dlg.center();
			}
		});
		permButtons.add(add);
		permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

		final Button addUser = new Button("Add User", new ClickListener() {

			public void onClick(Widget sender) {
				PermissionsAddDialog dlg = new PermissionsAddDialog(images, groups, permList, true);
				dlg.center();
			}
		});
		permButtons.add(addUser);
		permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

		permButtons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		permButtons.setSpacing(8);
		permButtons.addStyleName("gwt-TabPanelBottom");

		final Label readForAllNote = new Label("When this option is enabled, the file will be readable" +
					" by everyone. By checking this option, you are certifying that you have the right to " +
					"distribute this file and that it does not violate the Terms of Use.", true);
		readForAllNote.setVisible(false);
		readForAllNote.setStylePrimaryName("gss-readForAllNote");

		readForAll = new CheckBox();
		readForAll.setChecked(file.isReadForAll());
		readForAll.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if (readForAll.isChecked()) {
					readForAllNote.setVisible(true);
					pathPanel.setVisible(true);
				}
				else {
					readForAllNote.setVisible(false);
					pathPanel.setVisible(false);
				}
			}

		});

		permPanel.add(permList);
		permPanel.add(permButtons);
		// Only show the read for all permission if the user is the owner.
		if (file.getOwner().equals(GSS.get().getCurrentUserResource().getUsername())) {
			permForAll.add(new Label("Make Public"));
			permForAll.add(readForAll);
			permForAll.setSpacing(8);
			permForAll.addStyleName("gwt-TabPanelBottom");
			permPanel.add(permForAll);
			permPanel.add(readForAllNote);
		}

		TextBox path = new TextBox();
		path.addFocusListener(new FocusListener() {
			public void onFocus(Widget sender) {
				((TextBox) sender).selectAll();
			}
			public void onLostFocus(Widget sender) {
				((TextBox) sender).setSelectionRange(0, 0);
			}
		});
		path.setText(file.getPath());
		path.setTitle("Use this URI for sharing this file with the world");
		path.setReadOnly(true);
		pathPanel.add(new Label("Sharing URI"));
		pathPanel.setSpacing(8);
		pathPanel.addStyleName("gwt-TabPanelBottom");
		pathPanel.add(path);
		pathPanel.setVisible(file.isReadForAll());
		permPanel.add(pathPanel);

		VersionsList verList = new VersionsList(this, images, bodies);
		verPanel.add(verList);

		vPanel.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.setSpacing(8);
		vPanel.addStyleName("gwt-TabPanelBottom");
		vPanel.add(new Label("Versioned"));

		vPanel.add(versioned);
		verPanel.add(vPanel);
		vPanel2.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		vPanel2.setSpacing(8);
		vPanel2.addStyleName("gwt-TabPanelBottom");
		Button removeVersionsButton = new Button(images.delete().getHTML(), new ClickListener() {

			public void onClick(Widget sender) {
				ConfirmationDialog confirm = new ConfirmationDialog(images,"Really remove all previous versions?","Remove"){

					public void cancel() {
					}


					public void confirm() {
						FilePropertiesDialog.this.hide();
						removeAllOldVersions();
					}

				};
				confirm.center();
			}

		});
		HTML removeAllVersion = new HTML("<span>Remove all previous versions?</span>");
		vPanel2.add(removeAllVersion);
		vPanel2.add(removeVersionsButton);
		verPanel.add(vPanel2);
		if(!file.isVersioned())
			vPanel2.setVisible(false);
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
	private void updateTags() {
		ExecuteGet<TagsResource> tc = new ExecuteGet<TagsResource>(TagsResource.class, GSS.get().getCurrentUserResource().getTagsPath()) {

			public void onComplete() {
				allTagsContent.clear();
				TagsResource tagr = getResult();
				List<String> userTags = tagr.getTags();
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

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user tags");
			}
		};
		DeferredCommand.addCommand(tc);

	}

	@Override
	@SuppressWarnings("fallthrough")
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				accept();
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
	private void accept() {
		permList.updatePermissionsAccordingToInput();
		Set<PermissionHolder> perms = permList.getPermissions();
		JSONObject json = new JSONObject();
		if (!name.getText().equals(file.getName()))
			json.put("name", new JSONString(name.getText()));
		if (versioned.isChecked() != file.isVersioned())
			json.put("versioned", JSONBoolean.getInstance(versioned.isChecked()));
		//only update the read for all perm if the user is the owner
		if (readForAll.isChecked() != file.isReadForAll())
			if (file.getOwner().equals(GSS.get().getCurrentUserResource().getUsername()))
				json.put("readForAll", JSONBoolean.getInstance(readForAll.isChecked()));
		int i = 0;
		if (permList.hasChanges()) {
			GWT.log("Permissions change", null);
			JSONArray perma = new JSONArray();

			for (PermissionHolder p : perms) {
				JSONObject po = new JSONObject();
				if (p.getUser() != null)
					po.put("user", new JSONString(p.getUser()));
				if (p.getGroup() != null)
					po.put("group", new JSONString(p.getGroup()));
				po.put("read", JSONBoolean.getInstance(p.isRead()));
				po.put("write", JSONBoolean.getInstance(p.isWrite()));
				po.put("modifyACL", JSONBoolean.getInstance(p.isModifyACL()));
				perma.set(i, po);
				i++;
			}
			json.put("permissions", perma);
		}
		JSONArray taga = new JSONArray();
		i = 0;
		if (!tags.getText().equals(initialTagText)) {
			String[] tagset = tags.getText().split(",");
			for (String t : tagset) {
				JSONString to = new JSONString(t);
				taga.set(i, to);
				i++;
			}
			json.put("tags", taga);
		}
		String jsonString = json.toString();
		if(jsonString.equals("{}")){
			GWT.log("NO CHANGES", null);
			return;
		}
		ExecutePost cf = new ExecutePost(file.getPath() + "?update=", jsonString, 200) {

			public void onComplete() {
				GSS.get().getFileList().updateFileCache(true);
			}

			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						GSS.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						GSS.get().displayError("A file with the same name already exists");
					else if (statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
				} else
					GSS.get().displayError("System error modifying file:" + t.getMessage());
			}

		};
		DeferredCommand.addCommand(cf);

	}

	private void removeAllOldVersions() {
		JSONObject json = new JSONObject();
		json.put("versioned", JSONBoolean.getInstance(false));
		GWT.log(json.toString(), null);
		ExecutePost cf = new ExecutePost(file.getPath() + "?update=", json.toString(), 200) {

			public void onComplete() {
				toggleVersioned(true);
			}

			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						GSS.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						GSS.get().displayError("A folder with the same name already exists");
					else if (statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
				} else
					GSS.get().displayError("System error moifying file:" + t.getMessage());
			}
		};
		DeferredCommand.addCommand(cf);
	}

	private void toggleVersioned(boolean versionedValue) {
		JSONObject json = new JSONObject();
		json.put("versioned", JSONBoolean.getInstance(versionedValue));
		GWT.log(json.toString(), null);
		ExecutePost cf = new ExecutePost(file.getPath() + "?update=", json.toString(), 200) {

			public void onComplete() {
				GSS.get().getFileList().updateFileCache(true);
			}

			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						GSS.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						GSS.get().displayError("A folder with the same name already exists");
					else if (statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
				} else
					GSS.get().displayError("System error moifying file:" + t.getMessage());
			}
		};
		DeferredCommand.addCommand(cf);
	}

	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}

}
