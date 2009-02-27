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
package gr.ebs.gss.client.tree;

import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public abstract class Subtree {

	protected PopupTree tree;

	final Images images;

	public Subtree(PopupTree tree, final Images _images) {
		images = _images;
		this.tree = tree;

	}

	/**
	 * A helper method to simplify adding tree items that have attached images.
	 * {@link #addImageItem(TreeItem, String) code}
	 *
	 * @param parent the tree item to which the new item will be added.
	 * @param title the text associated with this item.
	 * @param imageProto the image of the item
	 * @return
	 */
	protected TreeItem addImageItem(final TreeItem parent, final String title, final AbstractImagePrototype imageProto, boolean draggable) {
		final DnDTreeItem item = new DnDTreeItem(imageItemHTML(imageProto, title), title, draggable);
		parent.addItem(item);
		return item;
	}



	/**
	 * Generates HTML for a tree item with an attached icon.
	 *
	 * @param imageProto the image icon
	 * @param title the title of the item
	 * @return the resultant HTML
	 */
	protected HTML imageItemHTML(final AbstractImagePrototype imageProto, final String title) {
		HTML html = new HTML("<a class='hidden-link' href='javascript:;'><span >" + imageProto.getHTML() + "&nbsp;" + title + "</span></a>");
		return html;
	}

	public void updateSubFoldersLazily(DnDTreeItem folderItem, List<FolderResource> subfolders, AbstractImagePrototype image) {
		for (int i = 0; i < folderItem.getChildCount(); i++) {
			DnDTreeItem c = (DnDTreeItem) folderItem.getChild(i);
			FolderResource f = (FolderResource) c.getUserObject();
			if (!listContainsFolder(f, subfolders)) {
				c.undoDraggable();
				folderItem.removeItem(c);
			}
		}

		LinkedList<DnDTreeItem> itemList = new LinkedList();
		for (FolderResource subfolder : subfolders) {
			DnDTreeItem item = folderItem.getChild(subfolder);
			if (item == null)
				item = (DnDTreeItem) addImageItem(folderItem, subfolder.getName(), image, true);
			else
				item.updateWidget(imageItemHTML(image, subfolder.getName()));
			item.setUserObject(subfolder);
			itemList.add(item);

		}
		for (DnDTreeItem it : itemList)
			it.remove();
		for (DnDTreeItem it : itemList)
			folderItem.addItem(it);
		for (int i = 0; i < folderItem.getChildCount(); i++) {
			DnDTreeItem c = (DnDTreeItem) folderItem.getChild(i);
			c.doDraggable();
			FolderResource f = (FolderResource) c.getUserObject();
		}
	}



	private boolean listContainsFolder(FolderResource folder, List<FolderResource> subfolders) {
		for (FolderResource f : subfolders)
			if (f.getPath().equals(folder.getPath()))
				return true;
		return false;
	}


}
