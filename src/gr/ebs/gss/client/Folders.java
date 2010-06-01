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

import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.tree.FolderSubtree;
import gr.ebs.gss.client.tree.MyShareSubtree;
import gr.ebs.gss.client.tree.OthersSharesSubtree;
import gr.ebs.gss.client.tree.TrashSubtree;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
/**
 * A tree displaying the folders in the user's file space.
 */
public class Folders extends Composite {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	/**
	 * Specifies the images that will be bundled for this Composite and other
	 * inherited images that will be included in the same bundle.
	 */
	public interface Images extends ClientBundle,Tree.Resources, FolderContextMenu.Images {

		@Source("gr/ebs/gss/resources/folder_home.png")
		ImageResource home();

		@Source("gr/ebs/gss/resources/folder_yellow.png")
		ImageResource folderYellow();

		@Source("gr/ebs/gss/resources/mimetypes/document.png")
		ImageResource document();

		@Source("gr/ebs/gss/resources/internet.png")
		ImageResource othersShared();

		@Source("gr/ebs/gss/resources/edit_user.png")
		ImageResource myShared();

		@Source("gr/ebs/gss/resources/folder_user.png")
		ImageResource sharedFolder();

		@Source("gr/ebs/gss/resources/trashcan_empty.png")
		ImageResource trash();
	}

	/**
	 * The widget's image bundle.
	 */
	private final Images images;

	/**
	 * The tree widget that displays the folder namespace.
	 */
	private PopupTree tree;

	/**
	 * A cached copy of the currently selected folder widget.
	 */
	private FolderSubtree folderSubtree;

	private TrashSubtree trashSubtree;

	private MyShareSubtree myShareSubtree;

	private OthersSharesSubtree othersSharesSubtree;

	/**
	 * Constructs a new folders widget with a bundle of images.
	 *
	 * @param _images a bundle that provides the images for this widget
	 */
	public Folders(final Images _images) {
		images = _images;
		tree = new PopupTree(images);
		tree.setAnimationEnabled(true);
		initWidget(tree);
		folderSubtree = new FolderSubtree(tree, images);
		myShareSubtree = new MyShareSubtree(tree, images);
		trashSubtree = new TrashSubtree(tree, images);
		othersSharesSubtree = new OthersSharesSubtree(tree, images);
	}

	public Images getImages() {
		return images;
	}

	public void select(TreeItem item) {
		tree.processItemSelected(item, true);
	}

	public void clearSelection() {
		tree.clearSelection();
	}

	public void update(TreeItem item) {
		if (isFileItem(item))
			folderSubtree.updateSubfolders((DnDTreeItem) item);
		else if (isTrash(item))
			trashSubtree.update();
		else if (isMySharedItem(item))
			myShareSubtree.update((DnDTreeItem) item);
		else if (isOthersSharedItem(item))
			othersSharesSubtree.update((DnDTreeItem) item);
	}

	public void updateFolder(final DnDTreeItem folderItem) {
		if (isFileItem(folderItem)){
			folderSubtree.updateFolderAndSubfolders(folderItem);
			myShareSubtree.updateFolderAndSubfolders((DnDTreeItem) getMySharesItem());
		}
		else if (isMySharedItem(folderItem)){
			myShareSubtree.updateFolderAndSubfolders(folderItem);
			if (folderItem.getFolderResource() != null) {
				DnDTreeItem fitem = (DnDTreeItem) getUserItem(getRootItem(), folderItem.getFolderResource().getUri());
				if (fitem != null)
					folderSubtree.updateFolderAndSubfolders(fitem);
				else
					folderSubtree.updateFolderAndSubfolders((DnDTreeItem) getRootItem());
			}

		}
		else if (isTrashItem(folderItem))
			trashSubtree.update();
		else if (isOthersSharedItem(folderItem))
			othersSharesSubtree.updateFolderAndSubfolders(folderItem);
	}

	/**
	 * Retrieve the current.
	 *
	 * @return the current
	 */
	public TreeItem getCurrent() {
		return tree.getTreeSelectedItem();
	}

	/**
	 * Modify the current.
	 *
	 * @param _current the current to set
	 */
	void setCurrent(final TreeItem _current) {
		tree.setTreeSelectedItem(_current);
	}

	/**
	 * Checks whether a TreeItem is contained in the root folder structure
	 *
	 * @param item The TreeItem to check
	 */
	public boolean isFileItem(TreeItem item) {
		if (getRootOfItem(item).equals(getRootItem()))
			return true;
		return false;
	}

	/**
	 * Checks whether a TreeItem is contained in the trash folder structure
	 *
	 * @param item The TreeItem to check
	 */
	public boolean isTrashItem(TreeItem item) {
		if (getRootOfItem(item).equals(getTrashItem()))
			return true;
		return false;
	}

	/**
	 * Checks whether a TreeItem is contained in the trash folder structure
	 *
	 * @param item The TreeItem to check
	 */
	public boolean isOthersSharedItem(TreeItem item) {
		if (getRootOfItem(item).equals(getSharesItem()))
			return true;
		return false;
	}

	/**
	 * Checks whether a TreeItem is contained in the trash folder structure
	 *
	 * @param item The TreeItem to check
	 */
	public boolean isMySharedItem(TreeItem item) {
		if (getRootOfItem(item).equals(getMySharesItem()))
			return true;
		return false;
	}

	private TreeItem getRootOfItem(TreeItem item) {
		if (item.getParentItem() == null)
			return item;
		TreeItem toCheck = item;
		while (toCheck.getParentItem() != null) {
			toCheck = toCheck.getParentItem();
			toCheck = getRootOfItem(toCheck);
		}
		return toCheck;
	}

	public TreeItem getUserOfSharedItem(TreeItem item) {
		if (item.getUserObject() instanceof OtherUserResource)
			return item;
		TreeItem test = item;
		while (test.getParentItem() != null) {
			test = test.getParentItem();
			if (test.getUserObject() instanceof OtherUserResource)
				return test;
		}
		return null;
	}

	public boolean isTrash(TreeItem item) {
		return item.equals(getTrashItem());
	}

	public boolean isMyShares(TreeItem item) {
		return item.equals(getMySharesItem());
	}

	public boolean isOthersShared(TreeItem item) {
		return item.equals(getSharesItem());
	}

	/*
	 * Returns the Tree Item corresponding to the FolderDTO object
	 * since we need to update main file structure for untrashed folders
	 */
	public TreeItem getUserItem(FolderResource folder) {
		return getUserItem(getRootItem(), folder);
	}

	public TreeItem getOtherSharedItem(FolderResource folder) {
		return getUserItem(getSharesItem(), folder);
	}

	private TreeItem getUserItem(TreeItem parent, FolderResource folder) {
		TreeItem tmp = null;
		if (parent.getUserObject() instanceof FolderResource &&
					(parent.getUserObject().equals(folder) ||
					((FolderResource) parent.getUserObject()).getUri().equals(folder.getUri())))
			return parent;
		for (int i = 0; i < parent.getChildCount(); i++) {
			TreeItem child = parent.getChild(i);
			if (child.getUserObject() instanceof FolderResource) {
				FolderResource dto = (FolderResource) child.getUserObject();
				if (dto.equals(folder) || dto.getUri().equals(folder.getUri()))
					return child;
			}
			tmp = getUserItem(child, folder);
			if (tmp != null)
				return tmp;
		}
		return null;
	}

	/**
	 * Retrieve the trashItem.
	 *
	 * @return the trashItem
	 */
	public TreeItem getTrashItem() {
		return trashSubtree.getRootItem();
	}

	/**
	 * Retrieve the rootItem.
	 *
	 * @return the rootItem
	 */
	public TreeItem getRootItem() {
		return folderSubtree.getRootItem();
	}

	/**
	 * Retrieve the mySharesItem.
	 *
	 * @return the mySharesItem
	 */
	public TreeItem getMySharesItem() {
		return myShareSubtree.getRootItem();
	}

	/**
	 * Retrieve the sharesItem.
	 *
	 * @return the sharesItem
	 */
	public TreeItem getSharesItem() {
		return othersSharesSubtree.getRootItem();
	}

	public void onFolderTrash(TreeItem folder) {
		if (folder.getParentItem().getUserObject() instanceof FolderResource) {
			FolderResource folderDTO = (FolderResource) folder.getParentItem().getUserObject();
			updateFileAndShareNodes(folderDTO);
		} else
			update(getMySharesItem());
		update(getTrashItem());
		clearSelection();
		GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
	}

	public void onFolderDelete(TreeItem folder) {
		if (folder.getParentItem().getUserObject() instanceof FolderResource) {
			FolderResource folderDTO = (FolderResource) folder.getParentItem().getUserObject();
			updateFileAndShareNodes(folderDTO);
		} else
			update(getMySharesItem());
		GSS.get().getStatusPanel().updateStats();
		clearSelection();
		GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
	}

	public void onFolderCopy(TreeItem folder) {
		if (!updateFileAndShareNodes((FolderResource) folder.getUserObject()))
			update(folder);
		GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
		GSS.get().getStatusPanel().updateStats();
	}

	public void onFolderMove(TreeItem folder, FolderResource initialParent) {
		updateFileAndShareNodes(initialParent);
		updateFileAndShareNodes((FolderResource) folder.getUserObject());
		update(folder);
		GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
		GSS.get().getStatusPanel().updateStats();
		clearSelection();
	}

	private boolean updateFileAndShareNodes(FolderResource folder) {
		boolean updated = false;
		TreeItem sharesFolder = getUserItem(getMySharesItem(), folder);
		if (sharesFolder != null) {
			update(sharesFolder);
			updated = true;
		}
		TreeItem fileFolder = getUserItem(getRootItem(), folder);
		if (fileFolder != null) {
			update(fileFolder);
			updated = true;
		}
		return updated;
	}

	public void initialize() {
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				GSS.get().showLoadingIndicator();
				folderSubtree.getRootItem().removeItems();
				trashSubtree.getRootItem().removeItems();
				myShareSubtree.getRootItem().removeItems();
				othersSharesSubtree.getRootItem().removeItems();
				update(folderSubtree.getRootItem());
				update(trashSubtree.getRootItem());
				update(myShareSubtree.getRootItem());
				update(othersSharesSubtree.getRootItem());
				GSS.get().setCurrentSelection(null);
				clearSelection();
				GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
				GSS.get().hideLoadingIndicator();
			}

		});
	}

	/* NEW HANDLING METHODS */
	public TreeItem getUserItem(TreeItem parent, String path) {
		TreeItem tmp = null;
		if (parent.getUserObject() instanceof RestResource && ((RestResource) parent.getUserObject()).getUri().equals(path))
			return parent;
		for (int i = 0; i < parent.getChildCount(); i++) {
			TreeItem child = parent.getChild(i);
			if (child.getUserObject() instanceof RestResource) {
				RestResource dto = (RestResource) child.getUserObject();
				if (dto.getUri().equals(path))
					return child;
			}
			tmp = getUserItem(child, path);
			if (tmp != null)
				return tmp;
		}
		return null;
	}

	public List<TreeItem> getItemsOfTreeForPath(String path) {
		List<TreeItem> result = new ArrayList<TreeItem>();
		TreeItem item = null;
		item = getUserItem(getRootItem(), path);
		if (item != null)
			result.add(item);
		item = getUserItem(getMySharesItem(), path);
		if (item != null)
			result.add(item);
		item = getUserItem(getTrashItem(), path);
		if (item != null)
			result.add(item);
		item = getUserItem(getSharesItem(), path);
		if (item != null)
			result.add(item);
		return result;
	}
	/**
	 *
	 * @return the popuptree
	 */
	public PopupTree getPopupTree(){
		return tree;
	}
}
