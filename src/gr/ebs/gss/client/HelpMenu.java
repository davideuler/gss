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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Help' menu implementation.
 */
public class HelpMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle{
		@Source("gr/ebs/gss/resources/khelpcenter.png")
		ImageResource userGuide();

		@Source("gr/ebs/gss/resources/linewidth.png")
		ImageResource terms();

		@Source("gr/ebs/gss/resources/bell.png")
		ImageResource reportAbuse();

		@Source("gr/ebs/gss/resources/bug.png")
		ImageResource reportBug();

		@Source("gr/ebs/gss/resources/info.png")
		ImageResource about();

		@Source("gr/ebs/gss/resources/edit_add.png")
		ImageResource upgradeQuota();
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

	@Override
	public void onClick(ClickEvent event) {
		HelpMenu menu = new HelpMenu(images);
		int left = event.getRelativeElement().getAbsoluteLeft();
		int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);
		Command hideCommand = new Command() {
			@Override
			public void execute() {
				hide();
			}
		};
		Command aboutCommand = new Command(){
			@Override
			public void execute() {
				AboutDialog dlg = new AboutDialog();
				dlg.center();
			}
		};
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.userGuide()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/userguide/el' target='_blank'>User Guide</a></span>", true, hideCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.terms()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/terms' target='_blank'>Terms &amp; Conditions</a></span>", true, hideCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.reportAbuse()).getHTML() + "&nbsp;<a class='hidden-link' " +
				"href='/report-abuse' target='_blank'>Report abuse</a></span>", true, hideCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.upgradeQuota()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/pithos/coupon' target='_blank'>Upgrade quota</a></span>", true, hideCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.reportBug()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='http://code.google.com/p/gss/issues/list' target='_blank'>Report bug</a></span>", true, hideCommand);
		contextMenu.addItem("<span>" + AbstractImagePrototype.create(images.about()).getHTML() + "&nbsp;About</span>", true, aboutCommand);
		return contextMenu;
	}

}
