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
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
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
	private void createGroup( final String groupName) {
		if (groupName == null || groupName.length() == 0) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		GWT.log("createGroup(" + groupName + ")", null);
		ExecutePost cg = new ExecutePost(GSS.get().getCurrentUserResource().getGroupsPath()+"?name="+groupName, "", 201){

			public void onComplete() {
				GSS.get().getGroups().updateGroups();
				GSS.get().showUserList();
			}

			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("Resource does not exist");
					else if(statusCode == 409)
						GSS.get().displayError("A group with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to create group:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error creating group:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(cg);
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
