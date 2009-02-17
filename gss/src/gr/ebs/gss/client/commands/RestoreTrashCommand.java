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
package gr.ebs.gss.client.commands;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


/**
 *
 * Restore trashed files and folders
 * @author kman
 *
 */
public class RestoreTrashCommand implements Command{
	private PopupPanel containerPanel;

	public RestoreTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null){
			//check to see if Trash Node is selected
			final List folderList = new ArrayList();
			TreeItem trashItem = GSS.get().getFolders().getTrashItem();
			for(int i=0 ; i < trashItem.getChildCount() ; i++)
				folderList.add(trashItem.getChild(i).getUserObject());
			if(GSS.get().getFolders().getCurrent().equals(GSS.get().getFolders().getTrashItem()))
				GSS.get().getRemoteService().restoreTrash(GSS.get().getCurrentUser().getId(), new AsyncCallback() {

					public void onSuccess(final Object result) {
						for(int k=0 ; k < folderList.size(); k++){
							FolderDTO fdto = (FolderDTO)folderList.get(k);
							GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getUserItem(fdto.getParent()));
						}
						GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getTrashItem());
						GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getMySharesItem());
						GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());

					}

					public void onFailure(final Throwable caught) {
						GWT.log("", caught);
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
					}
				});
			return;
		}
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FileHeaderDTO) {
			FileHeaderDTO fdto = (FileHeaderDTO) selection;
			fdto.setDeleted(false);
			GSS.get().getRemoteService().removeFileFromTrash(GSS.get().getCurrentUser().getId(), fdto.getId(), new AsyncCallback() {

				public void onSuccess(final Object result) {
					GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());

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
		else if (selection instanceof List) {
			List<FileHeaderDTO> fdtos = (List<FileHeaderDTO>) selection;
			final List<Long> fileIds = new ArrayList<Long>();
			for(FileHeaderDTO f : fdtos)
				fileIds.add(f.getId());
			GSS.get().getRemoteService().removeFilesFromTrash(GSS.get().getCurrentUser().getId(), fileIds, new AsyncCallback() {

				public void onSuccess(final Object result) {
					GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());

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
		else if (selection instanceof FolderDTO) {
			final FolderDTO fdto = (FolderDTO) selection;
			fdto.setDeleted(false);
			GSS.get().getRemoteService().removeFolderFromTrash(GSS.get().getCurrentUser().getId(), fdto.getId(), new AsyncCallback() {

				public void onSuccess(final Object result) {
					GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getTrashItem());
					GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getMySharesItem());
					GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getUserItem(fdto.getParent()));
					GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());

				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		} else if (selection instanceof UserDTO) {
			// TODO do we need trash in users?
		} else if (selection instanceof GroupDTO) {
			// TODO do we need trash for groups?
		}

	}

}
