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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'settings' menu implementation.
 */
public class SettingsMenu extends PopupPanel implements ClickListener {

	/**
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends MessagePanel.Images {

		/**
		 * Will bundle the file 'advancedsettings.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/advancedsettings.png")
		AbstractImagePrototype preferences();

		@Resource("gr/ebs/gss/resources/lock.png")
		AbstractImagePrototype credentials();

	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public SettingsMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		Command userCredentialsCommand = new Command(){
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.Command#execute()
			 */
			public void execute() {
				CredentialsDialog dlg = new CredentialsDialog(newImages);
				dlg.center();
			}
		};
		contextMenu = new MenuBar(true);
//		contextMenu.addItem("<span>" + newImages.preferences().getHTML() + "&nbsp;Preferences</span>", true, cmd);
		contextMenu.addItem("<span>" + newImages.credentials().getHTML() + "&nbsp;Show Credentials</span>", true, userCredentialsCommand);

		add(contextMenu);
		// setStyleName("toolbarPopup");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(final Widget sender) {
		final SettingsMenu menu = new SettingsMenu(images);
		final int left = sender.getAbsoluteLeft();
		final int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
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
