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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * The top panel, which contains the menu bar icons and the user name.
 */
public class TopPanel extends Composite {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle, FileMenu.Images, EditMenu.Images,
			SettingsMenu.Images, GroupMenu.Images, FilePropertiesDialog.Images,
			HelpMenu.Images {

		@Source("gr/ebs/gss/resources/exit.png")
		ImageResource exit();

		@Source("gr/ebs/gss/resources/folder_blue.png")
		ImageResource folder();

		@Source("gr/ebs/gss/resources/edit.png")
		ImageResource edit();

		@Source("gr/ebs/gss/resources/edit_group.png")
		ImageResource group();

		@Source("gr/ebs/gss/resources/configure.png")
		ImageResource configure();

		@Source("gr/ebs/gss/resources/help.png")
		ImageResource help();

		@Source("gr/ebs/gss/resources/pithos-logo.png")
		ImageResource gssLogo();

		@Source("gr/ebs/gss/resources/grnet-logo.png")
		ImageResource grnetLogo();
	}

	/**
	 * The menu bar widget.
	 */
	private MenuBar menu;

	/**
	 * The file menu widget.
	 */
	private FileMenu fileMenu;

	/**
	 * The edit menu widget.
	 */
	private EditMenu editMenu;

	/**
	 * The group menu widget.
	 */
	private GroupMenu groupMenu;

	/**
	 * The settings menu widget.
	 */
	private SettingsMenu settingsMenu;

	/**
	 * The help menu widget.
	 */
	private HelpMenu helpMenu;

	/**
	 * The constructor for the top panel.
	 *
	 * @param images the supplied images
	 */
	public TopPanel(Images images) {
		fileMenu = new FileMenu(images);
		editMenu = new EditMenu(images);
		groupMenu = new GroupMenu(images);
		settingsMenu = new SettingsMenu(images);
		helpMenu = new HelpMenu(images);
		HorizontalPanel outer = new HorizontalPanel();

		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		menu = new MenuBar();
		menu.setWidth("100%");
		menu.setAutoOpen(false);
		menu.setAnimationEnabled(true);
		menu.setStyleName("toolbarmenu");

		Command quitCommand = new Command(){
			public void execute() {
				QuitDialog dlg = new QuitDialog();
				dlg.center();
			}
		};
		MenuItem quitItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.exit()).getHTML() + "</td><td>Quit</td></tr></table>", true, quitCommand);
		MenuItem fileItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.folder()).getHTML() + "</td><td>File</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return fileMenu.createMenu();
			}
		};
		MenuItem editItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.edit()).getHTML() + "</td><td>Edit</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return editMenu.createMenu();
			}
		};
		MenuItem groupItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.group()).getHTML() + "</td><td>Group</td></tr></table>", true,
					groupMenu.getContextMenu());
		MenuItem configureItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.configure()).getHTML() + "</td><td>Settings</td></tr></table>",
					true,settingsMenu.getContextMenu());
		MenuItem helpItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.help()).getHTML() + "</td><td>Help</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return helpMenu.createMenu();
			}
		};
		menu.addItem(quitItem);
		menu.addItem(fileItem);
		menu.addItem(editItem);
		menu.addItem(groupItem);
		menu.addItem(configureItem);
		menu.addItem(helpItem);

		outer.setSpacing(2);
		outer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		outer.setCellVerticalAlignment(menu, HasVerticalAlignment.ALIGN_MIDDLE);
		outer.add(menu);
		outer.setStyleName("toolbar");

		Configuration conf = (Configuration) GWT.create(Configuration.class);
		HTML logos = new HTML("<table><tr><td><a href='" + conf.serviceHome() +
					"' target='gss'>" +	AbstractImagePrototype.create(images.gssLogo()).getHTML() +
					"</a><a href='http://www.grnet.gr/' " +	"target='grnet'>" +
					AbstractImagePrototype.create(images.grnetLogo()).getHTML()+"</a></td></tr></table>");
		outer.add(logos);

		outer.setCellHorizontalAlignment(logos, HasHorizontalAlignment.ALIGN_RIGHT);

		initWidget(outer);
	}

	/**
	 * Retrieve the fileMenu.
	 *
	 * @return the fileMenu
	 */
	FileMenu getFileMenu() {
		return fileMenu;
	}

	/**
	 * Retrieve the editMenu.
	 *
	 * @return the editMenu
	 */
	EditMenu getEditMenu() {
		return editMenu;
	}
}
