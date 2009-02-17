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

import gr.ebs.gss.client.commands.NewGroupCommand;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.RpcException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'Group' menu implementation.
 */
public class GroupMenu extends PopupPanel implements ClickListener {

	/**
	 * The default name for a new group.
	 */
	private static final String NEW_GROUP_NAME = "New Group";

	/**
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ImageBundle {

		/**
		 * Will bundle the file 'groupevent.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/groupevent.png")
		AbstractImagePrototype groupNew();

		/**
		 * Will bundle the file 'view_text.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/view_text.png")
		AbstractImagePrototype viewText();

	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public GroupMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		// Make a command that we will execute from all leaves.
		final Command cmd = new Command() {

			public void execute() {
				hide();
				Window.alert("You selected a menu item!");
			}
		};



		contextMenu = new MenuBar(true);
		contextMenu.addItem("<span>" + newImages.groupNew().getHTML() + "&nbsp;New Group</span>", true, new NewGroupCommand(this,images));

		add(contextMenu);
		// setStyleName("toolbarPopup");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(final Widget sender) {
		final GroupMenu menu = new GroupMenu(images);
		final int left = sender.getAbsoluteLeft();
		final int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
		menu.setPopupPosition(left, top);

		menu.show();
	}

	/**
	 * Generate an RPC request to create a new group.
	 *
	 * @param userId the ID of the user whose namespace will be searched for
	 *            groups
	 * @param groupName the name of the group to create
	 */
	private void createGroup(final Long userId, final String groupName) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		if (groupName == null || groupName.length() == 0) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		GWT.log("createGroup(" + userId + "," + groupName + ")", null);
		service.createGroup(userId, groupName, new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getGroups().updateGroups(GSS.get().getCurrentUser().getId());
			}

			public void onFailure(final Throwable caught) {
				if (caught instanceof DuplicateNameException) {
					// If the supplied name already exists in the parent folder,
					// alter it until we get one that doesn't.
					String newName = null;
					if (groupName.equals(NEW_GROUP_NAME))
						newName = NEW_GROUP_NAME + " 1";
					else {
						final int num = Integer.parseInt(groupName.substring(groupName.lastIndexOf(' ')).trim());
						newName = NEW_GROUP_NAME + " " + (num + 1);
					}
					createGroup(userId, newName);
				} else {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			}
		});
	}


	/**
	 * Retrieve the contextMenu.
	 *
	 * @return the contextMenu
	 */
	public MenuBar getContextMenu() {
		contextMenu.setAutoOpen(false);
		return contextMenu;
	}



}
