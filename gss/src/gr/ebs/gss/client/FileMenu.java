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

import gr.ebs.gss.client.commands.EmptyTrashCommand;
import gr.ebs.gss.client.commands.NewFolderCommand;
import gr.ebs.gss.client.commands.PropertiesCommand;
import gr.ebs.gss.client.commands.UploadFileCommand;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.UserDTO;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File' menu implementation.
 */
public class FileMenu extends PopupPanel implements ClickListener {

	/**
	 * The path info portion of the URL that provides the file download service.
	 */
	static final String FILE_DOWNLOAD_PATH = "/gss/fileDownload";

	/**
	 * The widget's images.
	 */
	private final Images images;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends FilePropertiesDialog.Images {

		/**
		 * Will bundle the file 'folder_new.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/folder_new.png")
		AbstractImagePrototype folderNew();

		/**
		 * Will bundle the file 'filenew.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/filenew.png")
		AbstractImagePrototype fileNew();

		/**
		 * Will bundle the file 'view_text.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/view_text.png")
		AbstractImagePrototype viewText();

		/**
		 * Will bundle the file 'download_manager.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/download_manager.png")
		AbstractImagePrototype download();

		/**
		 * Will bundle the file 'trashcan_empty.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/trashcan_empty.png")
		AbstractImagePrototype emptyTrash();
	}

	final MenuBar contextMenu = new MenuBar(true);

	/**
	 * The widget's constructor.
	 *
	 * @param _images the image bundle passed on by the parent object
	 */
	public FileMenu(final Images _images) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = _images;
		// createMenu();
		add(contextMenu);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(final Widget sender) {
		final FileMenu menu = new FileMenu(images);
		final int left = sender.getAbsoluteLeft();
		final int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
		menu.setPopupPosition(left, top);

		menu.show();
	}

	/**
	 * Do some validation before downloading a file.
	 */
	void preDownloadCheck() {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null || !(selection instanceof FileHeaderDTO)) {
			GSS.get().displayError("You have to select a file first");
			return;
		}
	}

	/**
	 * Create a download link for the respective menu item, if the currently
	 * selected object is a file.
	 *
	 * @param link a String array with two elements that is modified so that the
	 *            first position contains the opening tag and the second one the
	 *            closing tag
	 */
	void createDownloadLink(String[] link) {
		Object selection = GSS.get().getCurrentSelection();
		if (selection != null && selection instanceof FileHeaderDTO) {
			FileHeaderDTO file = (FileHeaderDTO) selection;
			link[0] = "<a class='hidden-link' href='" + FileMenu.FILE_DOWNLOAD_PATH + "?userId=" + GSS.get().getCurrentUser().getId().toString() + "&fileId=" + file.getId() + "' target='_blank'>";
			link[1] = "</a>";
		}
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);
		final Command downloadCmd = new Command() {

			public void execute() {
				hide();
				preDownloadCheck();
			}
		};
		Folders folders = GSS.get().getFolders();
		TreeItem selectedItem = folders.getCurrent();
		boolean downloadVisible = GSS.get().getCurrentSelection() != null && GSS.get().getCurrentSelection() instanceof FileHeaderDTO;
		boolean propertiesNotVisible = selectedItem != null && (folders.isTrash(selectedItem) || folders.isMyShares(selectedItem) || folders.isOthersShared(selectedItem) || selectedItem.getUserObject() instanceof UserDTO);
		contextMenu.addItem("<span>" + images.folderNew().getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(this, images));
		contextMenu.addItem("<span>" + images.fileNew().getHTML() + "&nbsp;New File</span>", true, new UploadFileCommand(this));
		contextMenu	.addItem("<span>" + images.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, images))
					.setVisible(!propertiesNotVisible);
		contextMenu.addItem("<span>" + images.emptyTrash().getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this));
		if (downloadVisible) {

			String[] link = {"", ""};
			createDownloadLink(link);
			contextMenu.addItem("<span>" + link[0] + images.download().getHTML() + "&nbsp;Download File" + link[1] + "</span>", true, downloadCmd);

		}
		return contextMenu;
	}

}
