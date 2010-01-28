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
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * A component that displays a list of the user's groups.
 */
public class Groups extends Composite implements SelectionHandler, OpenHandler {

	/**
	 * An image bundle for this widget.
	 */
	public interface Images extends Tree.Resources, ClientBundle, FileMenu.Images, EditMenu.Images, GroupMenu.Images, MessagePanel.Images {

		/**
		 * Will bundle the file 'groupevent.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/ebs/gss/resources/groupevent.png")
		ImageResource groupImage();

		@Source("gr/ebs/gss/resources/editdelete.png")
		ImageResource delete();

	}
	private boolean ctrlKeyPressed = false;

	private boolean leftClicked = false;

	private boolean rightClicked = false;

	/**
	 * cached latest group selection (for selecting and expanding on refresh)
	 */
	private String selectedGroup = null;

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

	private boolean showMenu=false;
	/**
	 * Constructs a new groups widget with a bundle of images.
	 *
	 * @param newImages a bundle that provides the images for this widget
	 */
	public Groups(final Images newImages) {

		images = newImages;
		menu = new GroupContextMenu(images);
		tree = new Tree(newImages);
		this.addHandler(new ContextMenuHandler() {

			@Override
			public void onContextMenu(ContextMenuEvent event) {
				if(current==null) return;
				int left = current.getAbsoluteLeft() + 40;
				int top = current.getAbsoluteTop() + 20;
				showPopup(left, top);

			}
		}, ContextMenuEvent.getType());
		tree.addSelectionHandler(this);
		tree.addOpenHandler(this);
		tree.setAnimationEnabled(true);
		initWidget(tree);
		setStylePrimaryName("gss-Groups");
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONDBLCLICK);
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
							final TreeItem item = new TreeItem();
							item.setWidget(imageItemHTML(images.groupImage(), groupList.get(i).getName(),item));
							item.setUserObject(groupList.get(i));
							tree.addItem(item);
							updateUsers(item);
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
	private TreeItem addImageItem(final TreeItem parent, final String title, final ImageResource imageProto) {
		final TreeItem item = new TreeItem();
		item.setWidget(imageItemHTML(imageProto, title,item));
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
	private HTML imageItemHTML(final ImageResource imageProto, final String title,final TreeItem item) {
		final HTML link = new HTML("<a class='hidden-link' href='javascript:;'>" + "<span>" + AbstractImagePrototype.create(imageProto).getHTML() + "&nbsp;" + title + "</span>" + "</a>"){
			@Override
			public void onBrowserEvent(Event event) {
				switch (DOM.eventGetType(event)) {
					case Event.ONMOUSEDOWN:
						if (DOM.eventGetButton(event) == NativeEvent.BUTTON_RIGHT || DOM.eventGetButton(event) == NativeEvent.BUTTON_LEFT)
							onSelection(item);
						break;
				}
				super.onBrowserEvent(event);

			}
		};
		link.sinkEvents(Event.ONMOUSEDOWN);
		return link;
	}



	protected void showPopup(final int x, final int y) {
		if (getCurrent() == null){
			GWT.log("[POPUP IS NULL]", null);
			return;
		}
		menu.hide();
		menu = new GroupContextMenu(images);
		menu.setPopupPosition(x, y);
		showMenu=false;
		menu.show();
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
					if (selectedGroup != null && groupItem.getText().equals(selectedGroup)) {
						//SelectionEvent.fire(tree, groupItem);;
						onSelection(groupItem);
						groupItem.setState(true);
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

	@Override
	public void onSelection(SelectionEvent event) {
		final TreeItem item = (TreeItem)event.getSelectedItem();
		onSelection(item);

	}

	private void onSelection(TreeItem item){
		final Object selected = item.getUserObject();
		// Preserve the previously selected item, so that the current's
		// onClick() method gets a chance to find it.
		if (getPrevious() != null)
			getPrevious().getWidget().removeStyleName("gss-SelectedRow");
		setCurrent(item);
		getCurrent().getWidget().addStyleName("gss-SelectedRow");
		setPrevious(getCurrent());
		GSS.get().setCurrentSelection(selected);
		//cache the latest top level node (group) for selecting and expanding on refresh
		if (item.getParentItem() == null)
			selectedGroup = item.getText();
		else
			selectedGroup = item.getParentItem().getText();
	}

	@Override
	public void onOpen(OpenEvent event) {
		final TreeItem item = (TreeItem) event.getTarget();
		setChanged(item);
		updateUsers(item);

	}
}
