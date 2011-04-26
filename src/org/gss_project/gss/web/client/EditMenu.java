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
package org.gss_project.gss.web.client;

import org.gss_project.gss.web.client.commands.CopyCommand;
import org.gss_project.gss.web.client.commands.CutCommand;
import org.gss_project.gss.web.client.commands.DeleteCommand;
import org.gss_project.gss.web.client.commands.PasteCommand;
import org.gss_project.gss.web.client.commands.ToTrashCommand;
import org.gss_project.gss.web.client.rest.resource.FileResource;

import org.gss_project.gss.web.client.rest.resource.GroupUserResource;
import org.gss_project.gss.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
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
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("org/gss_project/gss/resources/editcut.png")
		ImageResource cut();

		/**
		 * Will bundle the file 'editcopy.png' residing in the package
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("org/gss_project/gss/resources/editcopy.png")
		ImageResource copy();

		/**
		 * Will bundle the file 'editpaste.png' residing in the package
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("org/gss_project/gss/resources/editpaste.png")
		ImageResource paste();

		/**
		 * Will bundle the file 'editdelete.png' residing in the package
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Override
		@Source("org/gss_project/gss/resources/editdelete.png")
		ImageResource delete();

		/**
		 * Will bundle the file 'translate.png' residing in the package
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("org/gss_project/gss/resources/translate.png")
		ImageResource selectAll();

		/**
		 * Will bundle the file 'border_remove.png' residing in the package
		 * 'org.gss_project.gss.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("org/gss_project/gss/resources/border_remove.png")
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

			@Override
			public void execute() {
				hide();
				if(GSS.get().isFileListShowing())
					GSS.get().getFileList().selectAllRows();
				else if(GSS.get().isSearchResultsShowing())
					GSS.get().getSearchResults().selectAllRows();
			}
		};
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

		boolean cutcopyVisible = GSS.get().getCurrentSelection() != null && (GSS.get().getCurrentSelection() instanceof RestResourceWrapper
					|| GSS.get().getCurrentSelection() instanceof FileResource || GSS	.get().getCurrentSelection() instanceof GroupUserResource || GSS	.get().getCurrentSelection() instanceof List);
		String cutLabel = "Cut";
		String copyLabel ="Copy";
		String pasteLabel = "Paste";
		if(GSS.get().getCurrentSelection() != null)
			if(GSS.get().getCurrentSelection() instanceof RestResourceWrapper){
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
			else if(GSS.get().getClipboard().getItem().getRestResourceWrapper() != null)
				pasteLabel = "Paste Folder";
		MenuItem cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.cut()).getHTML() + "&nbsp;"+cutLabel+"</span>", true, new CutCommand(this));
		cutItem.getElement().setId("topMenu.edit.cut");
		contextMenu.addItem(cutItem).setVisible(cutcopyVisible);
		
		MenuItem copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.copy()).getHTML() + "&nbsp;"+copyLabel+"</span>", true, new CopyCommand(this));
		copyItem.getElement().setId("topMenu.edit.copy");
		contextMenu.addItem(copyItem).setVisible(cutcopyVisible);

		MenuItem pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;"+pasteLabel+"</span>", true, new PasteCommand(this));				
		pasteItem.getElement().setId("topMenu.edit.paste");
		if (GSS.get().getClipboard().getItem() != null)
			if(GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() == null){
				contextMenu.addItem(pasteItem);
			}
			else if(!GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() != null){
				//do not show paste
			}
			else if (GSS.get().getTreeView().getSelection() instanceof RestResourceWrapper){
				contextMenu.addItem(pasteItem);
			}
		MenuItem moveToTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
		moveToTrashItem.getElement().setId("topMenu.edit.moveToTrash");
		contextMenu	.addItem(moveToTrashItem)
					.setVisible(cutcopyVisible);
		
		MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
		deleteItem.getElement().setId("topMenu.edit.delete");
		contextMenu	.addItem(deleteItem)
					.setVisible(cutcopyVisible);
		
		MenuItem selectAllItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.selectAll()).getHTML() + "&nbsp;Select All</span>", true, selectAllCommand);
		selectAllItem.getElement().setId("topMenu.edit.selectAll");
		contextMenu.addItem(selectAllItem);
		
		MenuItem unSelectAllItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect All</span>", true, unselectAllCommand);
		unSelectAllItem.getElement().setId("topMenu.edit.unSelectAll");
		contextMenu.addItem(unSelectAllItem);
		return contextMenu;
	}



}
