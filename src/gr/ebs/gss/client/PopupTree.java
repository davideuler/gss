/*
 * Copyright 2008, 2009, 2010 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class PopupTree extends Tree {

	private FolderContextMenu menu;

	private Images images;

	private boolean ctrlKeyPressed = false;

	private boolean leftClicked = false;

	private boolean rightClicked = false;

	private TreeItem treeSelectedItem = null;

	public PopupTree(Images theImages) {
		super(theImages);
		images = theImages;
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
//		sinkEvents(Event.ONMOUSEDOWN);

		addSelectionHandler(new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem item = event.getSelectedItem();
				processItemSelected(item, true);

				String path = GSS.get().getApiPath() +  GSS.get().getCurrentUserResource().getUsername()+ "/";
//				Trash is selected
				if(GSS.get().getFolders().isTrash(item)){
					TrashResource currentObject = (TrashResource) GSS.get().getFolders().getCurrent().getUserObject();
					History.newItem("Trash"+currentObject.getUri().substring(path.lastIndexOf("/")));
				}
//				other's shared is selected
				else if (GSS.get().getFolders().isOthersShared(item)){
					OthersResource currentObject = (OthersResource) GSS.get().getFolders().getCurrent().getUserObject();
					History.newItem(currentObject.getUri().substring(path.lastIndexOf("/"))
									+ GSS.get().getFolders().getCurrent().getText());
				}
//				my shared is selected
				else if(GSS.get().getFolders().isMySharedItem(item)){
					SharedResource currentObject = (SharedResource) GSS.get().getFolders().getCurrent().getUserObject();
				History.newItem(currentObject.getUri().substring(path.lastIndexOf("/"))
								+ GSS.get().getFolders().getCurrent().getText());
				}
//				home folders are selected
				else{
					FolderResource currentObject = (FolderResource) GSS.get().getFolders().getCurrent().getUserObject();
					String uri = currentObject.getParentURI();
					if(uri == null)
						History.newItem("Files" + currentObject.getParentName());
					else
						History.newItem("Files"+ uri.substring(path.lastIndexOf("/")) + currentObject.getName());
				}

			}
		});

		addOpenHandler(new OpenHandler<TreeItem>() {

			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				TreeItem item = event.getTarget();
				if (item != null && item.getState())
					GSS.get().getFolders().update(item);

			}
		});
		addHandler(new ContextMenuHandler() {

			@Override
			public void onContextMenu(ContextMenuEvent event) {
				TreeItem item = getSelectedItem();
				if (item != null) {
					int left = item.getAbsoluteLeft() + 40;
					int top = item.getAbsoluteTop() + 20;
					showPopup(left, top);
				}

			}
		}, ContextMenuEvent.getType());
		// DOM.setStyleAttribute(getElement(), "position", "static");

	}

	@Override
	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONCLICK)
			return;

		switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				int key = DOM.eventGetKeyCode(event);
				if (key == KeyCodes.KEY_CTRL)
					ctrlKeyPressed = true;
				break;

			case Event.ONKEYUP:
				key = DOM.eventGetKeyCode(event);
				if (key == KeyCodes.KEY_CTRL)
					ctrlKeyPressed = false;
				break;

			case Event.ONMOUSEDOWN:
				if (DOM.eventGetButton(event) == NativeEvent.BUTTON_RIGHT)
					rightClicked = true;
				else if (DOM.eventGetButton(event) == NativeEvent.BUTTON_LEFT)
					leftClicked = true;
				break;

			case Event.ONMOUSEUP:
				if (DOM.eventGetButton(event) == NativeEvent.BUTTON_RIGHT)
					rightClicked = false;
				else if (DOM.eventGetButton(event) == NativeEvent.BUTTON_LEFT)
					leftClicked = false;
				break;
		}

		super.onBrowserEvent(event);
	}

	protected void showPopup(final int x, final int y) {
		if (treeSelectedItem == null)
			return;
		if (menu != null)
			menu.hide();
		menu = new FolderContextMenu(images);
		menu.setPopupPosition(x, y);
		menu.show();
	}

	public void processItemSelected(TreeItem item, @SuppressWarnings("unused") boolean fireEvents) {

		if (GSS.get().getCurrentSelection() == null || !GSS.get().getCurrentSelection().equals(item.getUserObject()))
			GSS.get().setCurrentSelection(item.getUserObject());
		if (!GSS.get().isFileListShowing())
			GSS.get().showFileList();

		// refresh Others Shared Node
		if (GSS.get().getFolders().isOthersShared(item)) {
			GSS.get().getFolders().update(item);
			GSS.get().showFileList();
		}
		// refresh Others Shared User Node
//		 else if(GSS.get().getFolders().isOthersSharedItem(item) &&
//		 item.getUserObject() instanceof UserDTO)
//		 GSS.get().getFolders().update(item);

		if (!item.equals(treeSelectedItem))
			processSelection(item);
		if (rightClicked) {
			rightClicked = false;
			int left = item.getAbsoluteLeft() + 40;
			int top = item.getAbsoluteTop() + 20;
			showPopup(left, top);
		} else if (leftClicked && ctrlKeyPressed) {
			leftClicked = false;
			ctrlKeyPressed = false;
			int left = item.getAbsoluteLeft() + 40;
			int top = item.getAbsoluteTop() + 20;
			showPopup(left, top);
		}
	}

	public void clearSelection() {
		if (treeSelectedItem != null)
			((DnDTreeItem) treeSelectedItem).getContent().removeStyleName("gss-SelectedRow");
		// treeSelectedItem.getWidget().removeStyleName("gss-SelectedRow");

		treeSelectedItem = null;
		setSelectedItem(null, true);
		GSS.get().setCurrentSelection(null);
	}

	private void processSelection(TreeItem item) {
		if (treeSelectedItem != null) {
			GSS.get().setCurrentSelection(null);
			// treeSelectedItem.getWidget().removeStyleName("gss-SelectedRow");
			((DnDTreeItem) treeSelectedItem).getContent().removeStyleName("gss-SelectedRow");
			treeSelectedItem = null;
			setSelectedItem(null, true);
		}
		treeSelectedItem = item;
		setSelectedItem(item, true);
		// ensureSelectedItemVisible();
		if (((DnDTreeItem) item).getFolderResource() != null)
			GSS.get().setCurrentSelection(((DnDTreeItem) item).getFolderResource());
		if (item.getUserObject() instanceof FolderResource)
			GSS.get().setCurrentSelection(item.getUserObject());
		else if (item.getUserObject() instanceof OtherUserResource)
			GSS.get().setCurrentSelection(item.getUserObject());
		else if (GSS.get().getFolders().isTrash(item))
			GSS.get().setCurrentSelection(null);
		// item.getWidget().addStyleName("gss-SelectedRow");
		((DnDTreeItem) item).getContent().addStyleName("gss-SelectedRow");
		// if(GSS.get().getFolders().isFileItem(item)||GSS.get().getFolders().isTrashItem(item)||GSS.get().getFolders().isMySharedItem(item))
		GSS.get().showFileList(true);
	}

	/**
	 * Retrieve the selectedItem.
	 *
	 * @return the selectedItem
	 */
	public TreeItem getTreeSelectedItem() {
		return treeSelectedItem;
	}

	/**
	 * Modify the selectedItem.
	 *
	 * @param newSelectedItem the selectedItem to set
	 */
	public void setTreeSelectedItem(TreeItem newSelectedItem) {
		treeSelectedItem = newSelectedItem;
	}

}
