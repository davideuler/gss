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

import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;
import gr.ebs.gss.client.rest.resource.GroupsResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * A component that displays a list of the user's groups.
 */
public class Groups extends Composite implements TreeListener {

	/**
	 * An image bundle for this widget.
	 */
	public interface Images extends TreeImages, FileMenu.Images, EditMenu.Images, GroupMenu.Images, MessagePanel.Images {

		/**
		 * Will bundle the file 'groupevent.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/groupevent.png")
		AbstractImagePrototype groupImage();

		@Resource("gr/ebs/gss/resources/editdelete.png")
		AbstractImagePrototype delete();
	}
	private boolean ctrlKeyPressed = false;

	private boolean leftClicked = false;

	private boolean rightClicked = false;


	/**
	 * The tree widget that displays the groups.
	 */
	private Tree tree;

	/**
	 * A cached copy of the currently selected group widget.
	 */
	private TreeItem current;

	/**
	 * A cached copy of the previously selected group widget.
	 */
	private TreeItem previous;

	/**
	 * A cached copy of the currently changed group widget.
	 */
	private TreeItem changed;

	/**
	 * The widget's image bundle.
	 */
	private final Images images;

	private GroupContextMenu menu;
	/**
	 * Constructs a new groups widget with a bundle of images.
	 *
	 * @param newImages a bundle that provides the images for this widget
	 */
	public Groups(final Images newImages) {

		images = newImages;
		menu = new GroupContextMenu(images);
		tree = new Tree(newImages);
		tree.addTreeListener(this);
		initWidget(tree);
		setStylePrimaryName("gss-Groups");
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
	}

	@Override
	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				int key = DOM.eventGetKeyCode(event);
				if (key == KeyboardListener.KEY_CTRL)
					ctrlKeyPressed = true;
				break;

			case Event.ONKEYUP:
				key = DOM.eventGetKeyCode(event);
				if (key == KeyboardListener.KEY_CTRL)
					ctrlKeyPressed = false;
				break;

			case Event.ONMOUSEDOWN:
				if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT)
					rightClicked = true;
				else if (DOM.eventGetButton(event) == Event.BUTTON_LEFT)
					leftClicked = true;
				break;

			case Event.ONMOUSEUP:
				if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT)
					rightClicked = false;
				else if (DOM.eventGetButton(event) == Event.BUTTON_LEFT)
					leftClicked = false;
				break;
		}

		super.onBrowserEvent(event);
	}
	/**
	 * Make an RPC call to retrieve the groups that belong to the specified
	 * user.
	 */
	public void updateGroups() {
		GetCommand<GroupsResource> gg = new GetCommand<GroupsResource>(GroupsResource.class, GSS.get().getCurrentUserResource().getGroupsPath()){

			@Override
			public void onComplete() {
				GroupsResource res = getResult();
				MultipleGetCommand<GroupResource> ga = new MultipleGetCommand<GroupResource>(GroupResource.class, res.getGroupPaths().toArray(new String[]{})){

					@Override
					public void onComplete() {
						List<GroupResource> groupList = getResult();
						tree.clear();
						for (int i = 0; i < groupList.size(); i++) {
							final TreeItem item = new TreeItem(imageItemHTML(images.groupImage(), groupList.get(i).getName()));
							item.setUserObject(groupList.get(i));
							tree.addItem(item);
							updateUsers( item);
						}
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
					}

					@Override
					public void onError(String p, Throwable throwable) {
						GWT.log("Path:"+p, throwable);
					}
				};
				DeferredCommand.addCommand(ga);
			}

			@Override
			public void onError(Throwable t) {

			}
		};
		DeferredCommand.addCommand(gg);
	}

	/**
	 *  update status panel with currently showing file stats
	 */
	public void updateCurrentlyShowingStats() {
		GSS.get().getStatusPanel().updateCurrentlyShowing(null); //clear stats - nothing to show for the groups tab
	}

	/**
	 * A helper method to simplify adding tree items that have attached images.
	 * {@link #addImageItem(TreeItem, String) code}
	 *
	 * @param parent the tree item to which the new item will be added.
	 * @param title the text associated with this item.
	 * @param imageProto
	 * @return the new tree item
	 */
	private TreeItem addImageItem(final TreeItem parent, final String title, final AbstractImagePrototype imageProto) {
		final TreeItem item = new TreeItem(imageItemHTML(imageProto, title));
		parent.addItem(item);
		return item;
	}

	/**
	 * Generates HTML for a tree item with an attached icon.
	 *
	 * @param imageProto the icon image
	 * @param title the title of the item
	 * @return the resultant HTML
	 */
	private HTML imageItemHTML(final AbstractImagePrototype imageProto, final String title) {
		final HTML link = new HTML("<a class='hidden-link' href='javascript:;'>" + "<span>" + imageProto.getHTML() + "&nbsp;" + title + "</span>" + "</a>");
		return link;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.TreeListener#onTreeItemSelected(com.google.gwt.user.client.ui.TreeItem)
	 */
	public void onTreeItemSelected(final TreeItem item) {
		final Object selected = item.getUserObject();
		// Preserve the previously selected item, so that the current's
		// onClick() method gets a chance to find it.
		if (getPrevious() != null)
			getPrevious().getWidget().removeStyleName("gss-SelectedRow");
		setCurrent(item);
		getCurrent().getWidget().addStyleName("gss-SelectedRow");
		setPrevious(getCurrent());
		GSS.get().setCurrentSelection(selected);
		if (rightClicked) {
			int left = item.getAbsoluteLeft() + 40;
			int top = item.getAbsoluteTop() + 20;
			showPopup(left, top);
		}
	}

	protected void showPopup(final int x, final int y) {
		if (getCurrent() == null)
			return;
		menu.hide();
		menu = new GroupContextMenu(images);
		menu.setPopupPosition(x, y);
		menu.show();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.TreeListener#onTreeItemStateChanged(com.google.gwt.user.client.ui.TreeItem)
	 */
	public void onTreeItemStateChanged(final TreeItem item) {
		// Ignore closed items.
		if (!item.getState())
			return;

		setChanged(item);
		updateUsers( item);
	}

	/**
	 * Generate an RPC request to retrieve the users of the specified group for
	 * display.
	 *
	 * @param userId the ID of the current user
	 * @param groupItem the TreeItem widget that corresponds to the requested
	 *            group
	 */
	void updateUsers(final TreeItem groupItem) {
		if(groupItem.getUserObject() instanceof GroupResource){
			GroupResource res = (GroupResource) groupItem.getUserObject();
			MultipleGetCommand<GroupUserResource> gu = new MultipleGetCommand<GroupUserResource>(GroupUserResource.class, res.getUserPaths().toArray(new String[]{})){
				@Override
				public void onComplete() {
					List<GroupUserResource> users = getResult();
					groupItem.removeItems();
					for (int i = 0; i < users.size(); i++) {
						final TreeItem userItem = addImageItem(groupItem, users.get(i).getName() + " &lt;" + users.get(i).getUsername() + "&gt;", images.permUser());
						userItem.setUserObject(users.get(i));
					}
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
				}

				@Override
				public void onError(String p, Throwable throwable) {
					GWT.log("Path:"+p, throwable);
				}
			};
			DeferredCommand.addCommand(gu);
		}

	}

	/**
	 * Retrieve the current.
	 *
	 * @return the current
	 */
	TreeItem getCurrent() {
		return current;
	}

	/**
	 * Modify the current.
	 *
	 * @param newCurrent the current to set
	 */
	void setCurrent(final TreeItem newCurrent) {
		current = newCurrent;
	}

	/**
	 * Modify the changed.
	 *
	 * @param newChanged the changed to set
	 */
	private void setChanged(final TreeItem newChanged) {
		changed = newChanged;
	}

	/**
	 * Retrieve the previous.
	 *
	 * @return the previous
	 */
	private TreeItem getPrevious() {
		return previous;
	}

	/**
	 * Modify the previous.
	 *
	 * @param newPrevious the previous to set
	 */
	private void setPrevious(final TreeItem newPrevious) {
		previous = newPrevious;
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible)
			updateGroups();
	}
}
