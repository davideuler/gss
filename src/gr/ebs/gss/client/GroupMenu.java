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
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ImageBundle {
		@Resource("gr/ebs/gss/resources/groupevent.png")
		AbstractImagePrototype groupNew();

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

		contextMenu = new MenuBar(true);
		contextMenu.addItem("<span>" + newImages.groupNew().getHTML() + "&nbsp;New Group</span>", true, new NewGroupCommand(this));

		add(contextMenu);
	}

	public void onClick(Widget sender) {
		GroupMenu menu = new GroupMenu(images);
		int left = sender.getAbsoluteLeft();
		int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
		menu.setPopupPosition(left, top);

		menu.show();
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
