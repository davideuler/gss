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
import gr.ebs.gss.client.GSSServiceAsync;
import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	public FolderSubtree(PopupTree tree, final Images _images) {
		super(tree, _images);
		tree.clear();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return fetchRootFolder();
			}
		});
	}

	/**
	 * @return
	 */
	public boolean fetchRootFolder() {
		UserDTO user = GSS.get().getCurrentUser();
		if (user == null)
			return !DONE;
		final Long userId = user.getId();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getRootFolder(userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final FolderDTO rootFolder = (FolderDTO) result;
				GWT.log("got folder '" + rootFolder.getName() + "'", null);
				rootItem = new DnDTreeItem(imageItemHTML(images.home(), rootFolder.getName()), rootFolder.getName(), false);
				rootItem.setUserObject(rootFolder);
				tree.addItem(rootItem);
				rootItem.doDroppable();
				//updateSubFolders(rootItem, rootFolder.getSubfolders(), rootFolder, images.folderYellow());
				updateSubFoldersLazily(rootItem, rootFolder.getSubfolders(), images.folderYellow());
				rootItem.setState(true);

			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});
		return DONE;
	}

	public void updateSubfolders(final Long userId, final TreeItem folderItem) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		if (!(folderItem.getUserObject() instanceof FolderDTO))
			return;
		final FolderDTO f = (FolderDTO) folderItem.getUserObject();
		final Long folderId = f.getId();
		service.getSubfolders(userId, folderId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List<FolderDTO> subfolders = (List) result;
				//updateSubFolders(folderItem, subfolders, images.folderYellow());
				updateSubFoldersLazily((DnDTreeItem) folderItem,  subfolders, images.folderYellow());
				GSS.get().hideLoadingIndicator();
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});

	}

	public void updateFolderAndSubfolders(final Long userId, final TreeItem folderItem) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		if (!(folderItem.getUserObject() instanceof FolderDTO))
			return;
		final FolderDTO f = (FolderDTO) folderItem.getUserObject();
		final Long folderId = f.getId();
		service.getFolderWithSubfolders(userId, folderId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final FolderDTO  folder = (FolderDTO) result;
				//updateSubFolders(folderItem, subfolders, images.folderYellow());
				updateFolderAndSubFoldersLazily((DnDTreeItem) folderItem, folder, folder.getSubfolders(), images.folderYellow());
				GSS.get().hideLoadingIndicator();
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});

	}

	public void updateNode(TreeItem node, FolderDTO folder) {
		node.getWidget().removeStyleName("gss-SelectedRow");
		if(node instanceof DnDTreeItem){
			((DnDTreeItem) node).undoDraggable();
			((DnDTreeItem) node).updateWidget(imageItemHTML(images.folderYellow(),folder.getName()));
			((DnDTreeItem) node).doDraggable();
		}
		else
			node.setWidget(imageItemHTML(images.folderYellow(),folder.getName()));
		node.setUserObject(folder);


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
