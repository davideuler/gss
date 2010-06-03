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
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
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
				GWT.log("====Inside PopupTree ====");
				GWT.log("item.getUserObject().toString() = '" +item.getUserObject().toString()+ "'");
				processItemSelected(item);
				String path = GSS.get().getApiPath() + GSS.get().getCurrentUserResource().getUsername()+ "/";
				((RestResource) GSS.get().getFolders().getCurrent().getUserObject()).updateHistory(item,path);
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

	public void processItemSelected(TreeItem item) {
 		if (GSS.get().getCurrentSelection() == null || !GSS.get().getCurrentSelection().equals(item.getUserObject()))
			GSS.get().setCurrentSelection(item.getUserObject());
		if (!GSS.get().isFileListShowing())
			GSS.get().showFileList();

		// refresh Others Shared Node
		if (GSS.get().getFolders().isOthersShared(item)){
			GSS.get().getFolders().update(item);
			GSS.get().showFileList();
		}

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
	/**
	 * Method that takes the current string uri, finds the corresponding treeItem object
	 * using the getTreeItem method and fires this treeItem object
	 * @param uri
	 * @return treeItem
	 */
//	#Files/files/home
	public TreeItem getTreeItemFromUri (String uri){
		String newUri = uri.substring(uri.indexOf("/")+1);
//		newUri = files/home
		String uri2 = newUri.substring(0,newUri.indexOf("/"));
		if(uri2.equalsIgnoreCase("files")){
//			folderResource
			String[] tokens = uri2.split("/");
			for (String t : tokens){
				TreeItem parentItem = getTreeItem(t);
				getTreeItemFromUri(parentItem.getText());
			}
		}
		else if(uri2.equalsIgnoreCase("trash")){
//			TrashResource currentObject = (TrashResource) GSS.get().getFolders().getCurrent().getUserObject();
//			return getTreeItem(uri2);
		}
		else if(uri2.equalsIgnoreCase("shared")){
			FolderResource currentObject = (FolderResource) GSS.get().getFolders().getCurrent().getUserObject();
			currentObject.getName();
		}
		else if(uri2.equalsIgnoreCase("others")){
			OtherUserResource currentObject = (OtherUserResource) GSS.get().getFolders().getCurrent().getUserObject();
			String objName = currentObject.getName();
			GWT.log("currentObject.getName() ='"+currentObject.getName()+"'");
			return getTreeItem(objName);

		}
		return getTreeItem(newUri);
	}

	/**
	 * Method that takes a folderName and finds the corresponding treeItem object and returns it
	 * @param historyToken
	 * @return treeItem
	 */

	public TreeItem getTreeItem (String historyToken){
		GWT.log("----Inside getTreeItem ----");
		String historyTokenOriginal = historyToken.replace("+", " ");
		GWT.log("historyTokenOriginal = '"+historyTokenOriginal+"'");

		String path = GSS.get().getApiPath() + GSS.get().getCurrentUserResource().getUsername()+ "/";
		Iterator<TreeItem> it = GSS.get().getFolders().getPopupTree().treeItemIterator() ;
		while(it.hasNext()){
			String name = "";
			TreeItem treeitem = it.next();
			if(treeitem.getUserObject() instanceof TrashResource){
				TrashResource currentObject = (TrashResource) treeitem.getUserObject();
				GWT.log("currentObject.toString() ='"+currentObject.toString() +"'");
				name = name +"Files/"+ currentObject.getUri().substring(path.lastIndexOf("/")+1);
//				"Files/"+ currentObject.getName();
				GWT.log("name = '"+name+"'");
			}
			if(treeitem.getUserObject() instanceof SharedResource){
				SharedResource currentObject = (SharedResource) treeitem.getUserObject();
				GWT.log("currentObject.toString() ='"+currentObject.toString() +"'");
				name = name +"Files/"+ currentObject.getUri().substring(path.lastIndexOf("/")+1);
				GWT.log("name = '"+name+"'");
			}
			if(treeitem.getUserObject() instanceof OthersResource){
				OthersResource currentObject = (OthersResource) treeitem.getUserObject();
				GWT.log("currentObject.toString() ='"+currentObject.toString() +"'");
				name = name + "Files/"+ path.substring(path.lastIndexOf("/")+1) + "others/";
				GWT.log("name = '"+name+"'");
			}
			if(treeitem.getUserObject() instanceof OtherUserResource){
				OtherUserResource currentObject = (OtherUserResource) treeitem.getUserObject();
				GWT.log("currentObject.toString() ='"+currentObject.toString() +"'");
				name = name + "Files/others/"+ currentObject.getName();
				GWT.log("name = '"+name+"'");
			}
			if(treeitem.getUserObject() instanceof FolderResource){
				FolderResource currentObject = (FolderResource) treeitem.getUserObject();
				GWT.log("currentObject.toString() ='"+currentObject.toString() +"'");
				GWT.log("currentObject.getParentURI() ='"+currentObject.getParentURI()+"'");
				if(currentObject.getParentURI() == null){
					if(containsFolder(treeitem, "Trash")){
//						case: selected folders below Trash folder
						String partialUri = constructPartialPath(treeitem);
						name = name +"Files/trash/" + partialUri;
						GWT.log("name = '"+name+"'");
					} else{
//						case: home folders are selected
						name = name + "Files/files/" + currentObject.getName();
						GWT.log("name = '"+name+"'");
					}
				}
				else if(treeitem.getParentItem() == null){
//					this is the case when the user uses the browser's forward arrow to navigate through other's
//					shared folders and	item.getParentItem is null only inside other's shared folder
					String apiPath = GSS.get().getApiPath();
					String newPath = currentObject.getParentURI().substring(apiPath.lastIndexOf("/"));
					name = name + "Files"+ newPath + currentObject.getName();
					GWT.log("name = '"+name+"'");
				}
				else if(containsFolder(treeitem, "My Shared")){
//					case: selected folders below My Shared folder
					String partialUri = constructPartialPath(treeitem);
					name = name + "Files/shared/" + partialUri;
					GWT.log("name = '"+name+"'");
				}else if(containsFolder(treeitem, "Other's Shared")){
//					case: selected folders below Other's Shared folder
					String partialPath = constructPartialPath(treeitem);
					name = name + "Files/others/"+ partialPath;
					GWT.log("name = '"+name+"'");
				}
				else{
//					case:all folders in user's folders tree
					String finalUri = currentObject.getParentURI().substring(path.lastIndexOf("/")) + currentObject.getName();
					name = name + "Files"+ finalUri;
					GWT.log("name = '"+name+"'");
				}
			}

			if(name.equals(historyTokenOriginal)){
				GWT.log("@@@treeitem.toString() = '" +treeitem.toString() +"'");
				return treeitem;
			}
			}
		return null;
	}

	public String getFolderName(String historyToken){
		String[] names = historyToken.split("/");
		return names[names.length -1];
	}
	/**
	 * construct the partial path of the selected TreeItem
	 *
	 * @param selectedItem the selectedItem to check
	 */
	private String constructPartialPath(TreeItem selectedItem){
	   String result = DisplayHelper.trim(selectedItem.getText());
	   TreeItem parent = selectedItem.getParentItem();
	   while (!(DisplayHelper.trim(parent.getText()).equals("My Shared") || DisplayHelper.trim(parent.getText()).equals("Other's Shared")||DisplayHelper.trim(parent.getText()).equals("Trash"))){
	      result = DisplayHelper.trim(parent.getText()) + "/" + result;
	      if(result.equals("My Shared")||result.equals("Other's Shared")) return result;
	      parent = parent.getParentItem();
	   }

	   return result;
	}
	/**
	 * examine whether a folder name like "Trash", "My Shared", "Other's Shared" is inside path
	 *
	 * @param selectedItem the selectedTreeItem to check
	 */

	private boolean containsFolder(TreeItem selectedItem, String folderName){
		TreeItem parent = selectedItem.getParentItem();
		while (parent != null){
			String parentItemText = parent.getText();
			String parentItemTextTr = DisplayHelper.trim(parentItemText);
			if(parentItemTextTr.equals(folderName)) return true;
			parent = parent.getParentItem();
			}
		return false;
	}
//	TODO when Groups or Search tab is selected then is there a TreeItem selected? guess not....

}
