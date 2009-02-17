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
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'Edit' menu implementation.
 */
public class EditMenu extends PopupPanel implements ClickListener {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private final MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends FileMenu.Images, MessagePanel.Images {

		/**
		 * Will bundle the file 'editcut.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/editcut.png")
		AbstractImagePrototype cut();

		/**
		 * Will bundle the file 'editcopy.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/editcopy.png")
		AbstractImagePrototype copy();

		/**
		 * Will bundle the file 'editpaste.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/editpaste.png")
		AbstractImagePrototype paste();

		/**
		 * Will bundle the file 'editdelete.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/editdelete.png")
		AbstractImagePrototype delete();

		/**
		 * Will bundle the file 'translate.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/translate.png")
		AbstractImagePrototype selectAll();

		/**
		 * Will bundle the file 'border_remove.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/border_remove.png")
		AbstractImagePrototype unselectAll();
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(final Widget sender) {
		final EditMenu menu = new EditMenu(images);
		final int left = sender.getAbsoluteLeft();
		final int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
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

		boolean cutcopyVisible = GSS.get().getCurrentSelection() != null && (GSS.get().getCurrentSelection() instanceof FolderDTO
					|| GSS.get().getCurrentSelection() instanceof FileHeaderDTO || GSS	.get().getCurrentSelection() instanceof UserDTO || GSS	.get().getCurrentSelection() instanceof List);
		contextMenu.addItem("<span>" + images.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this)).setVisible(cutcopyVisible);
		contextMenu.addItem("<span>" + images.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this)).setVisible(cutcopyVisible);
		if (GSS.get().getClipboard().getItem() != null)
			contextMenu.addItem("<span>" + images.paste().getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));
		contextMenu	.addItem("<span>" + images.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this))
					.setVisible(cutcopyVisible);
		contextMenu	.addItem("<span>" + images.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images))
					.setVisible(cutcopyVisible);
		contextMenu.addItem("<span>" + images.selectAll().getHTML() + "&nbsp;Select All</span>", true, selectAllCommand);
		contextMenu.addItem("<span>" + images.unselectAll().getHTML() + "&nbsp;Unselect All</span>", true, unselectAllCommand);
		return contextMenu;
	}

}
