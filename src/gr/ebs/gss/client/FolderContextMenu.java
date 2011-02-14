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
import gr.ebs.gss.client.rest.resource.MyFolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersFolderResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.SharedFolderResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashFolderResource;
import gr.ebs.gss.client.rest.resource.TrashResource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

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
		MenuBar contextMenu = new MenuBar(true);
		
		
		
		RestResource selectedItem = GSS.get().getTreeView().getSelection();


		if(selectedItem != null)
			if(selectedItem instanceof MyFolderResource){
				MenuItem newFolder = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolder.getElement().setId("folderContextMenu.newFolder");
				contextMenu.addItem(newFolder);
				
				MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				upload.getElement().setId("folderContextMenu.upload");
				contextMenu.addItem(upload);
								
				boolean notRootFolder = !GSS.get().getTreeView().getMyFolders().equals(selectedItem);
				if (notRootFolder) {
					// do not show the copy & cut option for the user's root folder					
					MenuItem cut = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
					cut.getElement().setId("folderContextMenu.cut");
					contextMenu.addItem(cut);
					
					MenuItem copy = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
					copy.getElement().setId("folderContextMenu.copy");
					contextMenu.addItem(copy);
					
				}
				contextMenu.addItem(pasteItem);
				if (notRootFolder) {
					// do not show delete options for the user's root folder
					MenuItem moveToTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
					moveToTrash.getElement().setId("folderContextMenu.moveToTrash");
					contextMenu.addItem(moveToTrash);
					
					MenuItem delete = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
					delete.getElement().setId("folderContextMenu.delete");
					contextMenu.addItem(delete);					
				}
				
				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.refresh");
				contextMenu.addItem(refresh);
				
				MenuItem sharing = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharing.getElement().setId("folderContextMenu.sharing");
				contextMenu.addItem(sharing);
				
				MenuItem properties = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				properties.getElement().setId("folderContextMenu.properties");
				contextMenu.addItem(properties);		
			}
		
			if(selectedItem instanceof SharedFolderResource){
				MenuItem newFolder = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolder.getElement().setId("folderContextMenu.newFolder");
				contextMenu.addItem(newFolder);
				
				MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				upload.getElement().setId("folderContextMenu.upload");
				contextMenu.addItem(upload);
				
				MenuItem cut = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				cut.getElement().setId("folderContextMenu.cut");
				contextMenu.addItem(cut);
				
				MenuItem copy = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				copy.getElement().setId("folderContextMenu.copy");
				contextMenu.addItem(copy);
				
				contextMenu.addItem(pasteItem);
				
				MenuItem moveToTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				moveToTrash.getElement().setId("folderContextMenu.moveToTrash");
				contextMenu.addItem(moveToTrash);
				
				MenuItem delete = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				delete.getElement().setId("folderContextMenu.delete");
				contextMenu.addItem(delete);

				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.refresh");
				contextMenu.addItem(refresh);
				
				MenuItem sharing = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharing.getElement().setId("folderContextMenu.sharing");
				contextMenu.addItem(sharing);
				
				MenuItem properties = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				properties.getElement().setId("folderContextMenu.properties");
				contextMenu.addItem(properties);
				
			}
			if(selectedItem instanceof TrashFolderResource){
				MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Restore folder and contents</span>", true, new RestoreTrashCommand(this));
				restore.getElement().setId("folderContextMenu.restore");
				contextMenu.addItem(restore);
				
				MenuItem delete = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				delete.getElement().setId("folderContextMenu.delete");
				contextMenu.addItem(delete);
				
				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.refresh");
				contextMenu.addItem(refresh);
				
			}
			if(selectedItem instanceof OthersFolderResource){				
				MenuItem newFolder = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				newFolder.getElement().setId("folderContextMenu.newFolder");
				contextMenu.addItem(newFolder);

				MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				upload.getElement().setId("folderContextMenu.upload");
				contextMenu.addItem(upload);
				
				MenuItem cut = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				cut.getElement().setId("folderContextMenu.cut");
				contextMenu.addItem(cut);
				
				MenuItem copy = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				copy.getElement().setId("folderContextMenu.copy");
				contextMenu.addItem(copy);
				
				contextMenu.addItem(pasteItem);
				
				MenuItem moveToTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				moveToTrash.getElement().setId("folderContextMenu.moveToTrash");
				contextMenu.addItem(moveToTrash);
				
				MenuItem delete = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				delete.getElement().setId("folderContextMenu.delete");
				contextMenu.addItem(delete);

				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.delete");
				contextMenu.addItem(refresh);
				
				MenuItem sharing = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				sharing.getElement().setId("folderContextMenu.sharing");
				contextMenu.addItem(sharing);
				
				MenuItem properties = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
				properties.getElement().setId("folderContextMenu.properties");
				contextMenu.addItem(properties);
				
			}
			if(selectedItem instanceof TrashResource){
				MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Restore Trash</span>", true, new RestoreTrashCommand(this));
				restore.getElement().setId("folderContextMenu.restore");
				contextMenu.addItem(restore);
				
				MenuItem emptyTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this));
				emptyTrash.getElement().setId("folderContextMenu.emptyTrash");
				contextMenu.addItem(emptyTrash);
				
				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.refresh");
				contextMenu.addItem(refresh);
				
			}
			if(selectedItem instanceof SharedResource || selectedItem instanceof OthersResource || selectedItem instanceof OtherUserResource){
				MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				refresh.getElement().setId("folderContextMenu.refresh");
				contextMenu.addItem(refresh);
				
			}
			
			/*
			if(folders.isTrashItem(selectedItem)){
				if (folders.isTrash(selectedItem)){
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this));
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				}
				else {
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Restore folder and contents</span>", true, new RestoreTrashCommand(this));
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				}
			}
			else if(folders.isFileItem(selectedItem)){
				
			}
			else if(!folders.isMyShares(selectedItem) && folders.isMySharedItem(selectedItem)){
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				contextMenu.addItem(pasteItem);
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
			}
			else if(!folders.isOthersShared(selectedItem) && folders.isOthersSharedItem(selectedItem) && !(GSS.get().getCurrentSelection() instanceof OtherUserResource)){
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
				contextMenu.addItem(pasteItem);
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, newImages));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, newImages, 1));
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, newImages, 0));
			} else if(!selectedItem.equals(folders.getSharesItem()))
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
			*/
		
		add(contextMenu);
		if (GSS.get().getClipboard().hasFolderOrFileItem())
			pasteItem.setVisible(true);
		else
			pasteItem.setVisible(false);
	}

}
