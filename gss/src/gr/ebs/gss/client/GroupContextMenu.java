/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.Groups.Images;
import gr.ebs.gss.client.commands.CopyCommand;
import gr.ebs.gss.client.commands.DeleteUserOrGroupCommand;
import gr.ebs.gss.client.commands.NewGroupCommand;
import gr.ebs.gss.client.commands.NewUserCommand;
import gr.ebs.gss.client.commands.PasteCommand;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


/**
 * @author kman
 *
 */
public class GroupContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;
	private MenuItem copy;
	private MenuItem paste;


	public GroupContextMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		images=newImages;
		setAnimationEnabled(true);
		final MenuBar contextMenu = new MenuBar(true);
		contextMenu.addItem("<span>" + newImages.groupNew().getHTML() + "&nbsp;New Group</span>", true, new NewGroupCommand(this,images));
		contextMenu.addItem("<span>" + newImages.groupNew().getHTML() + "&nbsp;Add User</span>", true, new NewUserCommand(this,images));
		copy = new MenuItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy User</span>", true, new CopyCommand(this));
		contextMenu.addItem(copy);
		paste = new MenuItem("<span>" + newImages.paste().getHTML() + "&nbsp;Paste User</span>", true, new PasteCommand(this));
		contextMenu.addItem(paste);
		contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteUserOrGroupCommand(this,images));
		add(contextMenu);

	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#show()
	 */
	@Override
	public void show() {
		TreeItem current = GSS.get().getGroups().getCurrent();
		if(current !=null && current.getUserObject() instanceof GroupUserResource && GSS.get().getCurrentSelection() instanceof GroupUserResource)
			copy.setVisible(true);
		else
			copy.setVisible(false);
		if(current !=null && current.getUserObject() instanceof GroupResource && GSS.get().getCurrentSelection() instanceof GroupResource && GSS.get().getClipboard().hasUserItem())
			paste.setVisible(true);
		else
			paste.setVisible(false);
		super.show();
	}

}
