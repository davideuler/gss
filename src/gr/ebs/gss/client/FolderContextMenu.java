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
import gr.ebs.gss.client.commands.RefreshCommand;
import gr.ebs.gss.client.commands.RestoreTrashCommand;
import gr.ebs.gss.client.commands.ToTrashCommand;
import gr.ebs.gss.client.commands.UploadFileCommand;
import gr.ebs.gss.client.rest.resource.OtherUserResource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
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
	public interface Images extends ClientBundle,FileMenu.Images, EditMenu.Images {
	}

	private MenuItem pasteItem;

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

		pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));
		pasteItem.getElement().setId("folderContextMenu.paste");
			
		MenuBar contextMenu = new MenuBar(true);
		Folders  folders = GSS.get().getFolders();
		TreeItem selectedItem = folders.getCurrent();


		if(selectedItem != null)
			if(folders.isTrashItem(selectedItem)){
				if (folders.isTrash(selectedItem)){
					MenuItem emptyTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this));
					emptyTrashItem.getElement().setId("folderContextMenu.emptyTrash");
					
					MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
					refreshItem.getElement().setId("folderContextMenu.refresh");
					
					contextMenu.addItem(emptyTrashItem);
					contextMenu.addItem(refreshItem);
				}
				else {
					MenuItem restoreFolderItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Restore folder and contents</span>", true, new RestoreTrashCommand(this));
					restoreFolderItem.getElement().setId("folderContectMenu.restoreFolder");
					
					MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
					deleteItem.getElement().setId("folderContextMenu.delete");
					
					MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
					refreshItem.getElement().setId("folderContextMenu.refresh");
					
					contextMenu.addItem(restoreFolderItem);
					contextMenu.addItem(deleteItem);
					contextMenu.addItem(refreshItem);
				}
			}
			else if(folders.isFileItem(selectedItem)){
				MenuItem newFolderItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolderItem.getElement().setId("folderContextMenu.newFolder");
				
				MenuItem uploadItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				uploadItem.getElement().setId("folderContextMenu.upload");
				
				contextMenu.addItem(newFolderItem);
				contextMenu.addItem(uploadItem);
				
				boolean notRootFolder = !folders.getRootItem().equals(selectedItem);
				if (notRootFolder) {
					// do not show the copy & cut option for the user's root folder
					MenuItem cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
					cutItem.getElement().setId("folderContextMenu.cut");
					
					MenuItem copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
					copyItem.getElement().setId("folderContextMenu.copy");
					
					contextMenu.addItem(cutItem);
					contextMenu.addItem(copyItem);
				}
				contextMenu.addItem(pasteItem);
				if (notRootFolder) {
					// do not show delete options for the user's root folder
					MenuItem moveToTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
					moveToTrashItem.getElement().setId("folderContextMenu.moveToTrash");
									
					MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
					deleteItem.getElement().setId("folderContextMenu.delete");
					
					contextMenu.addItem(moveToTrashItem);
					contextMenu.addItem(deleteItem);
				}
				MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refreshItem.getElement().setId("folderContextMenu.refresh");
				
				MenuItem sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharingItem.getElement().setId("folderContextMenu.sharing");
								
				MenuItem propertiesItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				propertiesItem.getElement().setId("folderContextMenu.properties");
				
				contextMenu.addItem(refreshItem);
				contextMenu.addItem(sharingItem);
				contextMenu.addItem(propertiesItem);
			}
			else if(!folders.isMyShares(selectedItem) && folders.isMySharedItem(selectedItem)){
				MenuItem newFolderItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolderItem.getElement().setId("folderContextMenu.newFolder");
												
				MenuItem uploadItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				uploadItem.getElement().setId("folderContextMenu.upload");
				
				MenuItem cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				cutItem.getElement().setId("folderContextMenu.cut");
				
				MenuItem copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				copyItem.getElement().setId("folderContextMenu.copy");
											
				MenuItem moveToTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				moveToTrashItem.getElement().setId("folderContextMenu.moveToTrash");
											
				MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				deleteItem.getElement().setId("folderContextMenu.delete");
				
				MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refreshItem.getElement().setId("folderContextMenu.refresh");
				
				MenuItem sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharingItem.getElement().setId("folderContextMenu.sharing");
				
				MenuItem propertiesItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				propertiesItem.getElement().setId("folderContextMenu.properties");
								
				contextMenu.addItem(newFolderItem);
				contextMenu.addItem(uploadItem);
				contextMenu.addItem(cutItem);
				contextMenu.addItem(copyItem);
				contextMenu.addItem(pasteItem);
				contextMenu.addItem(moveToTrashItem);
				contextMenu.addItem(deleteItem);
				contextMenu.addItem(refreshItem);
				contextMenu.addItem(sharingItem);
				contextMenu.addItem(propertiesItem);
			}
			else if(!folders.isOthersShared(selectedItem) && folders.isOthersSharedItem(selectedItem) && !(GSS.get().getCurrentSelection() instanceof OtherUserResource)){
				MenuItem newFolderItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolderItem.getElement().setId("folderContextMenu.newFolder");
								
				MenuItem uploadItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				uploadItem.getElement().setId("folderContextMenu.upload");
				
				MenuItem cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				cutItem.getElement().setId("folderContextMenu.cut");
								
				MenuItem copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				copyItem.getElement().setId("folderContextMenu.copy");
								
				MenuItem moveToTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				moveToTrashItem.getElement().setId("folderContextMenu.moveToTrash");
				
				MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				deleteItem.getElement().setId("folderContextMenu.delete");
				
				MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refreshItem.getElement().setId("folderContextMenu.refresh");
				
				MenuItem sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharingItem.getElement().setId("folderContextMenu.sharing");
				
				MenuItem propertiesItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				propertiesItem.getElement().setId("folderContectMenu.properties");
								
				contextMenu.addItem(newFolderItem);
				contextMenu.addItem(uploadItem);
				contextMenu.addItem(cutItem);
				contextMenu.addItem(copyItem);
				contextMenu.addItem(pasteItem);
				contextMenu.addItem(moveToTrashItem);
				contextMenu.addItem(deleteItem);
				contextMenu.addItem(refreshItem);
				contextMenu.addItem(sharingItem);
				contextMenu.addItem(propertiesItem);
				
			} else if(!selectedItem.equals(folders.getSharesItem())){
				MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refreshItem.getElement().setId("folderContextMenu.refresh");

				contextMenu.addItem(refreshItem);
			}
		add(contextMenu);
		if (GSS.get().getClipboard().hasFolderOrFileItem())
			pasteItem.setVisible(true);
		else
			pasteItem.setVisible(false);
	}

}
