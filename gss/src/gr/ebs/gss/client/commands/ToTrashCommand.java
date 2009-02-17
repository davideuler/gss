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
 * Move file or folder to trash
 * @author kman
 *
 */
public class ToTrashCommand implements Command{
	private PopupPanel containerPanel;

	public ToTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FolderDTO) {
			FolderDTO fdto = (FolderDTO) selection;
			fdto.setDeleted(true);
			GSS.get().getRemoteService().moveFolderToTrash(GSS.get().getCurrentUser().getId(), fdto.getId(), new AsyncCallback() {

				public void onSuccess(final Object result) {
					TreeItem folder = GSS.get().getFolders().getCurrent();
					GSS.get().getFolders().onFolderTrash(folder);
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		} else if (selection instanceof FileHeaderDTO) {
			FileHeaderDTO fdto = (FileHeaderDTO) selection;
			fdto.setDeleted(true);
			GSS.get().getRemoteService().moveFileToTrash(GSS.get().getCurrentUser().getId(), fdto.getId(), new AsyncCallback() {

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
			GSS.get().getRemoteService().moveFilesToTrash(GSS.get().getCurrentUser().getId(), fileIds, new AsyncCallback() {

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
		else if (selection instanceof UserDTO) {
			// TODO do we need trash in users?
		} else if (selection instanceof GroupDTO) {
			// TODO do we need trash for groups?
		}
	}

}
