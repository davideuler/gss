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
public class OthersSharesSubtree extends Subtree {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private DnDTreeItem rootItem;

	public OthersSharesSubtree(PopupTree tree, final Images _images) {
		super(tree, _images);

		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return updateInit();
			}
		});
	}

	public boolean updateInit() {
		UserDTO user = GSS.get().getCurrentUser();
		if (user == null || GSS.get().getFolders().getRootItem() == null || GSS.get().getFolders().getTrashItem() == null || GSS.get()
																																.getFolders()
																																.getMySharesItem() == null)
			return !DONE;
		Long userId = user.getId();

		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getUsersSharingFoldersForUser(userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List<UserDTO> subfolders = (List<UserDTO>) result;
				GWT.log("sub:" + subfolders.size(), null);
				rootItem = new DnDTreeItem(imageItemHTML(images.othersShared(), "Other's Shared"), "Other's Shared", false);
				rootItem.setUserObject("Shared");
				tree.addItem(rootItem);
				rootItem.removeItems();

				for (int i = 0; i < subfolders.size(); i++) {

					final UserDTO subfolder = subfolders.get(i);

					final TreeItem item = addImageItem(rootItem, subfolder.getUsername(), images.folderYellow(), false);
					item.setUserObject(subfolder);

				}
				GSS.get().hideLoadingIndicator();
				GSS.get().removeGlassPanel();
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				GSS.get().hideLoadingIndicator();
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});
		return DONE;

	}

	public void update(final TreeItem folderItem) {

		UserDTO user = GSS.get().getCurrentUser();
		final Long userId = user.getId();
		if (GSS.get().getFolders().isOthersShared(folderItem)) {
			GSS.get().showLoadingIndicator();
			final GSSServiceAsync service = GSS.get().getRemoteService();
			service.getUsersSharingFoldersForUser(userId, new AsyncCallback() {

				public void onSuccess(final Object result) {
					final List<UserDTO> subfolders = (List<UserDTO>) result;
					GWT.log("sub:" + subfolders.size(), null);
					rootItem.removeItems();
					for (int i = 0; i < subfolders.size(); i++) {
						final UserDTO subfolder = subfolders.get(i);
						final TreeItem item = addImageItem(rootItem, subfolder.getUsername(), images.folderYellow(), false);
						item.setUserObject(subfolder);
						doJob(item, userId, subfolder);
					}
					// mySharesItem.setState(true);
					GSS.get().hideLoadingIndicator();
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					GSS.get().hideLoadingIndicator();
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		} else if (GSS.get().getFolders().isOthersSharedItem(folderItem))
			if (folderItem.getUserObject() instanceof UserDTO) {

				final UserDTO ownerDTO = (UserDTO) folderItem.getUserObject();
				GSS.get().showLoadingIndicator();
				final GSSServiceAsync service = GSS.get().getRemoteService();
				service.getSharedRootFolders(ownerDTO.getId(), userId, new AsyncCallback() {

					public void onSuccess(final Object result) {
						final List<FolderDTO> subfolders = (List<FolderDTO>) result;
						GWT.log("sub:" + subfolders.size(), null);
						updateSubFoldersLazily((DnDTreeItem) folderItem, subfolders, images.folderYellow());
						GSS.get().hideLoadingIndicator();
					}

					public void onFailure(final Throwable caught) {
						GWT.log("", caught);
						GSS.get().hideLoadingIndicator();
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
					}
				});
			} else if (folderItem.getUserObject() instanceof FolderDTO) {
				final FolderDTO f = (FolderDTO) folderItem.getUserObject();
				final Long folderId = f.getId();
				GSS.get().showLoadingIndicator();
				final GSSServiceAsync service = GSS.get().getRemoteService();
				service.getSharedSubfolders(f.getOwner().getId(), userId, folderId, new AsyncCallback() {

					public void onSuccess(final Object result) {
						final List<FolderDTO> subfolders = (List) result;
						updateSubFoldersLazily((DnDTreeItem) folderItem, subfolders, images.folderYellow());
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

	}

	public void updateFolderAndSubfolders(final Long userId, final TreeItem folderItem) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		if (!(folderItem.getUserObject() instanceof FolderDTO))
			return;
		final FolderDTO f = (FolderDTO) folderItem.getUserObject();
		final Long folderId = f.getId();
		service.getFolderWithSubfolders(f.getOwner().getId(), userId, folderId, new AsyncCallback() {

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

	private void doJob(final TreeItem folderItem, Long userId, UserDTO ownerDTO) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getSharedRootFolders(ownerDTO.getId(), userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List<FolderDTO> subfolders = (List<FolderDTO>) result;
				updateSubFoldersLazily((DnDTreeItem) folderItem, subfolders, images.folderYellow());
				GSS.get().hideLoadingIndicator();
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				GSS.get().hideLoadingIndicator();
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});
	}

	/**
	 * Retrieve the rootItem.
	 *
	 * @return the rootItem
	 */
	public TreeItem getRootItem() {
		return rootItem;
	}

	public void updateNode(TreeItem node, FolderDTO folder) {
		node.getWidget().removeStyleName("gss-SelectedRow");
		if (node instanceof DnDTreeItem){
			((DnDTreeItem) node).undoDraggable();
			((DnDTreeItem) node).updateWidget(imageItemHTML(images.folderYellow(), folder.getName()));
			((DnDTreeItem) node).doDraggable();
		}
		else
			node.setWidget(imageItemHTML(images.folderYellow(), folder.getName()));
		node.setUserObject(folder);
	}
}
