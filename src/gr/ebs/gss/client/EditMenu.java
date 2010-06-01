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
import gr.ebs.gss.client.commands.ToTrashCommand;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Edit' menu implementation.
 */
public class EditMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private final MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle, FileMenu.Images, MessagePanel.Images {

		/**
		 * Will bundle the file 'editcut.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/editcut.png")
		ImageResource cut();

		/**
		 * Will bundle the file 'editcopy.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/editcopy.png")
		ImageResource copy();

		/**
		 * Will bundle the file 'editpaste.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/editpaste.png")
		ImageResource paste();

		/**
		 * Will bundle the file 'editdelete.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/editdelete.png")
		ImageResource delete();

		/**
		 * Will bundle the file 'translate.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/translate.png")
		ImageResource selectAll();

		/**
		 * Will bundle the file 'border_remove.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/border_remove.png")
		ImageResource unselectAll();
	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public EditMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
		createMenu();
		add(contextMenu);
	}

	@Override
	public void onClick(ClickEvent event) {
		final EditMenu menu = new EditMenu(images);
		final int left = event.getRelativeElement().getAbsoluteLeft();
		final int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);

		final Command selectAllCommand = new Command() {

			public void execute() {
				hide();
				if(GSS.get().isFileListShowing())
					GSS.get().getFileList().selectAllRows();
				else if(GSS.get().isSearchResultsShowing())
					GSS.get().getSearchResults().selectAllRows();
			}
		};
		final Command unselectAllCommand = new Command() {

			public void execute() {
				hide();
				if(GSS.get().isFileListShowing())
					GSS.get().getFileList().clearSelectedRows();
				else if(GSS.get().isSearchResultsShowing())
					GSS.get().getSearchResults().clearSelectedRows();
			}
		};

		boolean cutcopyVisible = GSS.get().getCurrentSelection() != null && (GSS.get().getCurrentSelection() instanceof FolderResource
					|| GSS.get().getCurrentSelection() instanceof FileResource || GSS	.get().getCurrentSelection() instanceof GroupUserResource || GSS	.get().getCurrentSelection() instanceof List);
		String cutLabel = "Cut";
		String copyLabel ="Copy";
		String pasteLabel = "Paste";
		if(GSS.get().getCurrentSelection() != null)
			if(GSS.get().getCurrentSelection() instanceof FolderResource){
				cutLabel = "Cut Folder";
				copyLabel = "Copy Folder";
			}
			else if(GSS.get().getCurrentSelection() instanceof FileResource){
				cutLabel = "Cut File";
				copyLabel = "Copy File";
			}
			else if(GSS.get().getCurrentSelection() instanceof List){
				cutLabel = "Cut Files";
				copyLabel = "Copy Files";
			}
		if(GSS.get().getClipboard().getItem() != null)
			if(GSS.get().getClipboard().getItem().getFile() != null)
				pasteLabel = "Paste File";
			else if(GSS.get().getClipboard().getItem().getFiles() != null)
				pasteLabel = "Paste Files";
			else if(GSS.get().getClipboard().getItem().getFolderResource() != null)
				pasteLabel = "Paste Folder";
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.cut()).getHTML() + "&nbsp;"+cutLabel+"</span>", true, new CutCommand(this)).setVisible(cutcopyVisible);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.copy()).getHTML() + "&nbsp;"+copyLabel+"</span>", true, new CopyCommand(this)).setVisible(cutcopyVisible);
		if (GSS.get().getClipboard().getItem() != null)
			if(GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() == null)
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;"+pasteLabel+"</span>", true, new PasteCommand(this));
			else if(!GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() != null){
				//do not show paste
			}
			else if (GSS.get().getFolders().getCurrent().getUserObject() instanceof FolderResource)
				contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;"+pasteLabel+"</span>", true, new PasteCommand(this));
		contextMenu	.addItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this))
					.setVisible(cutcopyVisible);
		contextMenu	.addItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images))
					.setVisible(cutcopyVisible);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.selectAll()).getHTML() + "&nbsp;Select All</span>", true, selectAllCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect All</span>", true, unselectAllCommand);
		return contextMenu;
	}



}
