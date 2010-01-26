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

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class FolderSubtree extends Subtree {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private DnDTreeItem rootItem;

	public FolderSubtree(PopupTree aTree, final Images _images) {
		super(aTree, _images);
		aTree.clear();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return fetchRootFolder();
			}
		});
	}

	public boolean fetchRootFolder() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null)
			return !DONE;

		final String path = userResource.getFilesPath();
		GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path) {

			@Override
			public void onComplete() {
				FolderResource rootResource = getResult();
				rootItem = new DnDTreeItem(imageItemHTML(images.home(), rootResource.getName()), false,tree);
				rootItem.setUserObject(rootResource);
				tree.clear();
				tree.addItem(rootItem);
				rootItem.doDroppable();
				GSS.get().getFolders().select(rootItem);
				updateSubFoldersLazily(rootItem, rootResource.getFolders(), images.folderYellow(), images.sharedFolder());
				rootItem.setState(true);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching root folder", t);
				GSS.get().displayError("Unable to fetch root folder");
				if(rootItem != null){
					rootItem = new DnDTreeItem(imageItemHTML(images.home(), "ERROR"), false,tree);
					tree.clear();
					tree.addItem(rootItem);
				}
			}

		};
		DeferredCommand.addCommand(gf);
		return DONE;
	}

	public void updateSubfolders(final DnDTreeItem folderItem) {
		if (folderItem.getFolderResource() == null) {
			GWT.log("folder resource is null", null);
			return;
		}
		updateNodes(folderItem);
	}

	private void updateNodes(final DnDTreeItem folderItem) {
		String parentName = "";
		if (folderItem.getParentItem() != null)
			parentName = ((DnDTreeItem) folderItem.getParentItem()).getFolderResource().getName() + "->";
		parentName = parentName + folderItem.getFolderResource().getName();
		MultipleGetCommand<FolderResource> gf = new MultipleGetCommand<FolderResource>(FolderResource.class,
					folderItem.getFolderResource().getSubfolderPaths().toArray(new String[] {})) {

			@Override
			public void onComplete() {
				List<FolderResource> res = getResult();
				folderItem.getFolderResource().setFolders(res);
				updateSubFoldersLazily(folderItem, res, images.folderYellow(), images.sharedFolder());
				for (int i = 0; i < folderItem.getChildCount(); i++) {
					DnDTreeItem anItem = (DnDTreeItem) folderItem.getChild(i);
					updateSubFoldersLazily(anItem, anItem.getFolderResource().getFolders(), images.folderYellow(), images.sharedFolder());
					anItem.setState(false);
				}
			}

			@Override
			public void onError(Throwable t) {
				GSS.get().displayError("Unable to fetch subfolders");
				GWT.log("Unable to fetch subfolders", t);
			}

			@Override
			public void onError(String p, Throwable throwable) {
				GWT.log("Path:"+p, throwable);
			}

		};
		DeferredCommand.addCommand(gf);
	}

	public void updateFolderAndSubfolders(final DnDTreeItem folderItem) {
		final String path = folderItem.getFolderResource().getUri();
		GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path) {

			@Override
			public void onComplete() {
				FolderResource rootResource = getResult();
				if (!folderItem.equals(rootItem)) {
					folderItem.undoDraggable();
					if(rootResource.isShared())
						folderItem.updateWidget(imageItemHTML(images.sharedFolder(), rootResource.getName()));
					else
						folderItem.updateWidget(imageItemHTML(images.folderYellow(), rootResource.getName()));
					folderItem.setUserObject(rootResource);
					folderItem.doDraggable();
				} else{
					folderItem.undoDroppable();
					folderItem.setUserObject(rootResource);
					folderItem.updateWidget(imageItemHTML(images.home(), rootResource.getName()));
					folderItem.doDroppable();
				}
				updateSubfolders(folderItem);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching folder", t);
				GSS.get().displayError("Unable to fetch folder:" + folderItem.getFolderResource().getName());
			}
		};
		DeferredCommand.addCommand(gf);
	}

	/**
	 * Retrieve the rootItem.
	 *
	 * @return the rootItem
	 */
	public TreeItem getRootItem() {
		return rootItem;
	}

}
