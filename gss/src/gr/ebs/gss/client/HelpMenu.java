/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
import com.google.gwt.user.client.ui.ImageBundle.Resource;

/**
 * The 'Help' menu implementation.
 */
public class HelpMenu extends PopupPanel implements ClickListener {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images {
		@Resource("gr/ebs/gss/resources/linewidth.png")
		AbstractImagePrototype terms();

		@Resource("gr/ebs/gss/resources/info.png")
		AbstractImagePrototype about();
	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public HelpMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
		createMenu();
		add(contextMenu);
	}

	public void onClick(Widget sender) {
		HelpMenu menu = new HelpMenu(images);
		int left = sender.getAbsoluteLeft();
		int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);
		Command termsCommand = new Command() {
			public void execute() {
				hide();
			}
		};
		Command aboutCommand = new Command(){
			public void execute() {
				AboutDialog dlg = new AboutDialog();
				dlg.center();
			}
		};
		contextMenu.addItem("<span>" + images.terms().getHTML() + "&nbsp;<a class='hidden-link' " +
				"href='/terms' target='_blank'>Terms &amp; Conditions</a></span>", true, termsCommand);
		contextMenu.addItem("<span>" + images.about().getHTML() + "&nbsp;About</span>", true, aboutCommand);
		return contextMenu;
	}

}
