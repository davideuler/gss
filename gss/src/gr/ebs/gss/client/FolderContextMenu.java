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

import gr.ebs.gss.client.commands.CopyCommand;
import gr.ebs.gss.client.commands.CutCommand;
import gr.ebs.gss.client.commands.DeleteCommand;
import gr.ebs.gss.client.commands.EmptyTrashCommand;
import gr.ebs.gss.client.commands.NewFolderCommand;
import gr.ebs.gss.client.commands.PasteCommand;
import gr.ebs.gss.client.commands.PropertiesCommand;
import gr.ebs.gss.client.commands.RestoreTrashCommand;
import gr.ebs.gss.client.commands.ToTrashCommand;
import gr.ebs.gss.client.commands.UploadFileCommand;
import gr.ebs.gss.client.rest.resource.OtherUserResource;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * The 'Folder Context' menu implementation.
 */
public class FolderContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends FileMenu.Images, EditMenu.Images {
	}
	MenuItem pasteItem;
	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FolderContextMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		// A dummy command that we will execute from unimplemented leaves.
		final Command cmd = new Command() {

			public void execute() {
				hide();
				Window.alert("You selected a menu item!");
			}
		};
		pasteItem = new MenuItem("<span>" + newImages.paste().getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));
		final MenuBar contextMenu = new MenuBar(true);
		final Folders  folders = GSS.get().getFolders();
		final TreeItem selectedItem = folders.getCurrent();


		if(selectedItem != null)
			if(folders.isTrashItem(selectedItem)){
				boolean notTrashRootFolder = !folders.isTrash(selectedItem);
				contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this)).setVisible(!notTrashRootFolder);
				//'Restore'/'Delete' not in Trash root
				contextMenu.addItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Restore folder and contents</span>", true, new RestoreTrashCommand(this)).setVisible(notTrashRootFolder);
				contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages)).setVisible(notTrashRootFolder);
			}
			/*
			else if(((DnDTreeItem)selectedItem).getFolderResource()!=null){
				contextMenu.addItem("<span>" + newImages.folderNew().getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + newImages.fileNew().getHTML() + "&nbsp;New File</span>", true, new UploadFileCommand(this));
				// do not show the copy & cut option for the user's root folder
				contextMenu.addItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				contextMenu.addItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				contextMenu.addItem(pasteItem);
				// do not show delete options for the user's root folder
				contextMenu.addItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				contextMenu.addItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages));
			}*/
			else if(folders.isFileItem(selectedItem)){

				boolean notRootFolder = !folders.getRootItem().equals(selectedItem);
				contextMenu.addItem("<span>" + newImages.folderNew().getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + newImages.fileNew().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));
				// do not show the copy & cut option for the user's root folder
				contextMenu.addItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this)).setVisible(notRootFolder);
				contextMenu.addItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this)).setVisible(notRootFolder);
				contextMenu.addItem(pasteItem);
				// do not show delete options for the user's root folder
				contextMenu.addItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this)).setVisible(notRootFolder);
				contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages)).setVisible(notRootFolder);
				contextMenu.addItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages));
			}
			else if(!folders.isMyShares(selectedItem) && folders.isMySharedItem(selectedItem)){
				contextMenu.addItem("<span>" + newImages.folderNew().getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + newImages.fileNew().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));
				// do not show the copy & cut option for the user's root folder
				contextMenu.addItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				contextMenu.addItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				contextMenu.addItem(pasteItem);
				// do not show delete options for the user's root folder
				contextMenu.addItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				contextMenu.addItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages));
			}
			else if(!folders.isOthersShared(selectedItem) && folders.isOthersSharedItem(selectedItem) && !(GSS.get().getCurrentSelection() instanceof OtherUserResource)){
				contextMenu.addItem("<span>" + newImages.folderNew().getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + newImages.fileNew().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));
				// do not show the copy & cut option for the user's root folder
				//contextMenu.addItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				contextMenu.addItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				contextMenu.addItem(pasteItem);
				// do not show delete options for the user's root folder
				//contextMenu.addItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				//contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				contextMenu.addItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages));
			}


		add(contextMenu);
		if (GSS.get().getClipboard().hasFolderOrFileItem())
			pasteItem.setVisible(true);
		else
			pasteItem.setVisible(false);
	}




}
