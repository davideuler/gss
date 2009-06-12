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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File Context' menu implementation.
 */
public class FileContextMenu extends PopupPanel implements ClickListener {

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
	public interface Images extends FileMenu.Images, EditMenu.Images {

		@Resource("gr/ebs/gss/resources/mimetypes/document.png")
		AbstractImagePrototype fileContextMenu();

		@Resource("gr/ebs/gss/resources/doc_versions.png")
		AbstractImagePrototype versions();

		@Resource("gr/ebs/gss/resources/group.png")
		AbstractImagePrototype sharing();

		@Resource("gr/ebs/gss/resources/border_remove.png")
		AbstractImagePrototype unselectAll();
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

			public void execute() {
				hide();
				GSS.get().getTopPanel().getFileMenu().preDownloadCheck();
			}
		};

		pasteItem = new MenuItem("<span>" + newImages.paste().getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));

		MenuBar contextMenu = new MenuBar(true);
		if (isEmpty) {
			contextMenu.addItem(pasteItem);
			if (gss.getFolders().getCurrent() != null)
				if (gss.getFolders().isFileItem(gss.getFolders().getCurrent()))
					contextMenu.addItem("<span>" + newImages.fileUpdate().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));
				else if (gss.getFolders().isMySharedItem(gss.getFolders().getCurrent()) || GSS	.get()
																											.getFolders()
																											.isOthersSharedItem(GSS	.get()
																																	.getFolders()
																																	.getCurrent()))
					if(gss.getFolders().getCurrent().getUserObject() instanceof FolderResource)
						contextMenu.addItem("<span>" + newImages.fileUpdate().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));
			contextMenu.addItem("<span>" + images.refresh().getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
		} else if (isTrash) {
			contextMenu.addItem("<span>" + newImages.versions().getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(this));
			contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
		} else {
			final Command unselectAllCommand = new Command() {

				public void execute() {
					hide();
					if(GSS.get().isFileListShowing())
						GSS.get().getFileList().clearSelectedRows();
					else if(GSS.get().isSearchResultsShowing())
						GSS.get().getSearchResults().clearSelectedRows();
				}
			};
			cutItem = new MenuItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
			copyItem = new MenuItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));

			updateItem = new MenuItem("<span>" + newImages.fileUpdate().getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this, images));

			trashItem = new MenuItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
			deleteItem = new MenuItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));

			sharingItem = new MenuItem("<span>" + newImages.sharing().getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, images, 1));
			propItem = new MenuItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, images, 0));

			TreeItem currentFolder = gss.getFolders().getCurrent();
			if(currentFolder!=null && currentFolder.getUserObject() instanceof FolderResource)
				contextMenu.addItem(updateItem);
			String[] link = {"", ""};
			gss.getTopPanel().getFileMenu().createDownloadLink(link, false);
			downloadItem = new MenuItem("<span>" + link[0] + newImages.download().getHTML() + " Download" + link[1] + "</span>", true, downloadCmd);
			contextMenu.addItem(downloadItem);
			gss.getTopPanel().getFileMenu().createDownloadLink(link, true);
			saveAsItem = new MenuItem("<span>" + link[0] + newImages.download().getHTML() + " Save file as" + link[1] + "</span>", true, downloadCmd);
			contextMenu.addItem(saveAsItem);
			contextMenu.addItem(cutItem);
			contextMenu.addItem(copyItem);
			if(currentFolder!=null && currentFolder.getUserObject() instanceof FolderResource)
				contextMenu.addItem(pasteItem);
			contextMenu.addItem("<span>" + images.unselectAll().getHTML() + "&nbsp;Unselect</span>", true, unselectAllCommand);
			contextMenu.addItem(trashItem);
			contextMenu.addItem(deleteItem);
			contextMenu.addItem("<span>" + images.refresh().getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
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
		propItem.setVisible(false);
		downloadItem.setVisible(false);
		sharingItem.setVisible(false);
	}

	public void onClick(Widget sender) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = sender.getAbsoluteLeft();
				int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
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
				int left = sender.getAbsoluteLeft();
				int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
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
