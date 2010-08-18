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
import gr.ebs.gss.client.commands.PasteCommand;
import gr.ebs.gss.client.commands.PropertiesCommand;
import gr.ebs.gss.client.commands.RefreshCommand;
import gr.ebs.gss.client.commands.RestoreTrashCommand;
import gr.ebs.gss.client.commands.ToTrashCommand;
import gr.ebs.gss.client.commands.UploadFileCommand;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * The 'File Context' menu implementation.
 */
public class FileContextMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuItem cutItem;

	private MenuItem copyItem;

	private MenuItem pasteItem;

	private MenuItem updateItem;

	private MenuItem sharingItem;

	private MenuItem propItem;

	private MenuItem trashItem;

	private MenuItem deleteItem;

	private MenuItem downloadItem;

	private MenuItem saveAsItem;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends ClientBundle,FileMenu.Images, EditMenu.Images {

		@Source("gr/ebs/gss/resources/mimetypes/document.png")
		ImageResource fileContextMenu();

		@Source("gr/ebs/gss/resources/doc_versions.png")
		ImageResource versions();

		@Override
		@Source("gr/ebs/gss/resources/group.png")
		ImageResource sharing();

		@Override
		@Source("gr/ebs/gss/resources/border_remove.png")
		ImageResource unselectAll();

		@Source("gr/ebs/gss/resources/demo.png")
		ImageResource viewImage();
}

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FileContextMenu(Images newImages, boolean isTrash, boolean isEmpty) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		GSS gss = GSS.get();
		setAnimationEnabled(true);
		images = newImages;

		// The command that does some validation before downloading a file.
		Command downloadCmd = new Command() {

			@Override
			public void execute() {
				hide();
				GSS.get().getTopPanel().getFileMenu().preDownloadCheck();
			}
		};

		pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));

		MenuBar contextMenu = new MenuBar(true);
		if (isEmpty) {
			contextMenu.addItem(pasteItem);
			if (GSS.get().getFolders().getCurrent() != null)
				if (GSS.get().getFolders().isFileItem(GSS.get().getFolders().getCurrent()))
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				else if (GSS.get().getFolders().isMySharedItem(GSS.get().getFolders().getCurrent()) || GSS	.get()
																											.getFolders()
																											.isOthersSharedItem(GSS	.get()
																																	.getFolders()
																																	.getCurrent()))
					if(GSS.get().getFolders().getCurrent().getUserObject() instanceof FolderResource)
						contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
			contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
		} else if (isTrash) {
			contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(this));
			contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
		} else {
			final Command unselectAllCommand = new Command() {

				@Override
				public void execute() {
					hide();
					if(GSS.get().isFileListShowing())
						GSS.get().getFileList().clearSelectedRows();
					else if(GSS.get().isSearchResultsShowing())
						GSS.get().getSearchResults().clearSelectedRows();
				}
			};
			cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
			copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));

			updateItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));

			trashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
			deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));

			sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, images, 1));
			propItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, images, 0));

			TreeItem currentFolder = gss.getFolders().getCurrent();
			if(currentFolder!=null && currentFolder.getUserObject() instanceof FolderResource)
				contextMenu.addItem(updateItem);
			String[] link = {"", ""};
			gss.getTopPanel().getFileMenu().createDownloadLink(link, false);
			downloadItem = new MenuItem("<span>" + link[0] + AbstractImagePrototype.create(newImages.download()).getHTML() + " Download" + link[1] + "</span>", true, downloadCmd);
			contextMenu.addItem(downloadItem);
			gss.getTopPanel().getFileMenu().createDownloadLink(link, true);
			saveAsItem = new MenuItem("<span>" + link[0] + AbstractImagePrototype.create(newImages.download()).getHTML() + " Save As" + link[1] + "</span>", true, downloadCmd);
			contextMenu.addItem(saveAsItem);
			contextMenu.addItem(cutItem);
			contextMenu.addItem(copyItem);
			if(currentFolder!=null && currentFolder.getUserObject() instanceof FolderResource)
				contextMenu.addItem(pasteItem);
			contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect</span>", true, unselectAllCommand);
			contextMenu.addItem(trashItem);
			contextMenu.addItem(deleteItem);
			contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
			contextMenu.addItem(sharingItem);
			contextMenu.addItem(propItem);
		}
		add(contextMenu);
		if (gss.getClipboard().hasFileItem())
			pasteItem.setVisible(true);
		else
			pasteItem.setVisible(false);
	}

	void onMultipleSelection() {
		updateItem.setVisible(false);
		downloadItem.setVisible(false);
		saveAsItem.setVisible(false);
		sharingItem.setVisible(false);
	}
	@Override
	public void onClick(ClickEvent event) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getRelativeElement().getAbsoluteLeft();
				int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			} else if (GSS.get().getCurrentSelection() instanceof List) {
				FileContextMenu menu;
				if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}
				int left = event.getRelativeElement().getAbsoluteLeft();
				int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			}
	}

	public void onEvent(Event event) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getClientX();
				int top = event.getClientY();
				menu.setPopupPosition(left, top);
				menu.show();
			} else if (GSS.get().getCurrentSelection() instanceof List) {
				FileContextMenu menu;
				if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}
				int left = event.getClientX();
				int top = event.getClientY();
				menu.setPopupPosition(left, top);
				menu.show();
			}
	}

	public void onContextEvent(ContextMenuEvent event) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getNativeEvent().getClientX();
				int top = event.getNativeEvent().getClientY();
				menu.setPopupPosition(left, top);
				menu.show();

			} else if (GSS.get().getCurrentSelection() instanceof List) {
				FileContextMenu menu;
				if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}
				int left = event.getNativeEvent().getClientX();
				int top = event.getNativeEvent().getClientY();
				menu.setPopupPosition(left, top);
				menu.show();
			}
	}

	public void onContextEmptyEvent(ContextMenuEvent event) {
		FileContextMenu menu;
		if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
			menu = new FileContextMenu(images, true, true);
		else if(((DnDTreeItem)GSS.get().getFolders().getCurrent()).getFolderResource() != null)
			menu = new FileContextMenu(images, false, true);
		else return;
		int left = event.getNativeEvent().getClientX();
		int top = event.getNativeEvent().getClientY();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public void onEmptyEvent(Event event) {
		FileContextMenu menu;
		if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
			menu = new FileContextMenu(images, true, true);
		else if(((DnDTreeItem)GSS.get().getFolders().getCurrent()).getFolderResource() != null)
			menu = new FileContextMenu(images, false, true);
		else return;
		int left = event.getClientX();
		int top = event.getClientY();
		menu.setPopupPosition(left, top);
		menu.show();
	}


}
