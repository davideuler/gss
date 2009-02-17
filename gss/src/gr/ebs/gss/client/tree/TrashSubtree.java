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
 *
 */
public class TrashSubtree extends Subtree{
	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;
	private DnDTreeItem rootItem;


	public TrashSubtree(PopupTree tree, final Images _images){
		super(tree,_images);


		DeferredCommand.addCommand(new IncrementalCommand() {
			public boolean execute() {
				return updateInit();
			}
		});
	}

	public boolean updateInit() {
		UserDTO user = GSS.get().getCurrentUser();
		if (user == null || GSS.get().getFolders().getRootItem() == null) return !DONE;
		Long userId = user.getId();

		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getDeletedRootFolders(userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List<FolderDTO> subfolders = (List<FolderDTO>) result;
				rootItem  = new DnDTreeItem(imageItemHTML(images.trash(), "Trash"),"Trash", false);
				rootItem.setUserObject("Trash");
				tree.addItem(rootItem);
				rootItem.doDroppable();
				rootItem.removeItems();
				for (int i = 0; i < subfolders.size(); i++) {
					final FolderDTO subfolder = subfolders.get(i);
					final TreeItem item = addImageItem(rootItem, subfolder.getName(), images.folderYellow(), false);
					item.setUserObject(subfolder);
				}
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
		return DONE;

	}


	public boolean update(final TreeItem folderItem) {
		UserDTO user = GSS.get().getCurrentUser();
		if (user == null) return !DONE;
		Long userId = user.getId();

		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getDeletedRootFolders(userId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				final List<FolderDTO> subfolders = (List<FolderDTO>) result;
				rootItem.removeItems();
				for (int i = 0; i < subfolders.size(); i++) {
					final FolderDTO subfolder = subfolders.get(i);
					final TreeItem item = addImageItem(rootItem, subfolder.getName(), images.folderYellow(), false);
					item.setUserObject(subfolder);

				}
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
		return DONE;

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
