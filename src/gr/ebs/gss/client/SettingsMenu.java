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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'settings' menu implementation.
 */
public class SettingsMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle,MessagePanel.Images {

		/**
		 * Will bundle the file 'advancedsettings.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/advancedsettings.png")
		ImageResource preferences();

		@Source("gr/ebs/gss/resources/lock.png")
		ImageResource credentials();

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
			@Override
			public void execute() {
				CredentialsDialog dlg = new CredentialsDialog(newImages);
				dlg.center();
			}
		};
		contextMenu = new MenuBar(true);
//		contextMenu.addItem("<span>" + newImages.preferences().getHTML() + "&nbsp;Preferences</span>", true, cmd);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.credentials()).getHTML() + "&nbsp;Show Credentials</span>", true, userCredentialsCommand);

		add(contextMenu);
		// setStyleName("toolbarPopup");
	}

	@Override
	public void onClick(final ClickEvent event) {
		final SettingsMenu menu = new SettingsMenu(images);
		final int left = event.getRelativeElement().getAbsoluteLeft();
		final int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
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
