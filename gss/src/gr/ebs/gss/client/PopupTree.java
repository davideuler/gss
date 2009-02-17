/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

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

	public PopupTree(Images images) {
		super(images);
		this.images = images;
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				processItemSelected(item, true);
			}

			public void onTreeItemStateChanged(TreeItem item) {
				if (!item.getState()){
					GWT.log("Returning", null);
					return;
				}
				GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), item);
			}
		});
	}


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

	protected void showPopup(final int x, final int y) {
		if (treeSelectedItem == null)
			return;
		if(menu != null)
			menu.hide();
		menu = new FolderContextMenu(images);
		menu.setPopupPosition(x, y);
		menu.show();
	}

	void processItemSelected(TreeItem item, boolean fireEvents) {
		if(!GSS.get().isFileListShowing()){
			if(GSS.get().getCurrentSelection() == null || !GSS.get().getCurrentSelection().equals(item.getUserObject()))
				GSS.get().setCurrentSelection(item.getUserObject());
			GSS.get().showFileList();
		}

		//refresh Others Shared Node
		if(GSS.get().getFolders().isOthersShared(item))
			GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), item);
		//refresh Others Shared User Node
		else if(GSS.get().getFolders().isOthersSharedItem(item) && item.getUserObject() instanceof UserDTO)
			GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), item);

		if (!item.equals(treeSelectedItem))
			processSelection(item);
		if (rightClicked) {
			int left = item.getAbsoluteLeft() + 40;
			int top = item.getAbsoluteTop() + 20;
			showPopup(left, top);
		} else if (leftClicked && ctrlKeyPressed) {
			int left = item.getAbsoluteLeft() + 40;
			int top = item.getAbsoluteTop() + 20;
			showPopup(left, top);
		}
	}

	public void clearSelection(){
		if (treeSelectedItem != null)
			treeSelectedItem.getWidget().removeStyleName("gss-SelectedRow");
		treeSelectedItem = null;
		GSS.get().setCurrentSelection(null);
	}

	private void processSelection(TreeItem item) {
		if (treeSelectedItem != null) {
			GSS.get().setCurrentSelection(null);
			treeSelectedItem.getWidget().removeStyleName("gss-SelectedRow");

			treeSelectedItem = null;
		}
		treeSelectedItem = item;
		if (item.getUserObject() instanceof FolderDTO)
			GSS.get().setCurrentSelection(item.getUserObject());
		else if(item.getUserObject() instanceof UserDTO)
			GSS.get().setCurrentSelection(item.getUserObject());
		else if(GSS.get().getFolders().isTrash(item))
			GSS.get().setCurrentSelection(null);
		item.getWidget().addStyleName("gss-SelectedRow");
		//if(GSS.get().getFolders().isFileItem(item)||GSS.get().getFolders().isTrashItem(item)||GSS.get().getFolders().isMySharedItem(item))
			GSS.get().showFileList();
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
	 * @param treeSelectedItem the selectedItem to set
	 */
	public void setTreeSelectedItem(TreeItem newSelectedItem) {
		treeSelectedItem = newSelectedItem;
	}

}
