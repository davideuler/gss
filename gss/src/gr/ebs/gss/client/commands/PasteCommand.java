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
import gr.ebs.gss.client.GSSServiceAsync;
import gr.ebs.gss.client.clipboard.Clipboard;
import gr.ebs.gss.client.clipboard.ClipboardItem;
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
 * @author kman Command for pasting Clipboard contents
 */
public class PasteCommand implements Command {

	private PopupPanel containerPanel;

	public PasteCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		GWT.log("selection: " + selection, null);
		if (selection == null || GSS.get().getClipboard().getItem() == null)
			return;

		if (selection instanceof FolderDTO) {
			ClipboardItem citem = GSS.get().getClipboard().getItem();
			if (citem.getFile() != null) {
				if (citem.getOperation() == Clipboard.COPY)
					copyFile(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFile());
				else if (citem.getOperation() == Clipboard.CUT)
					moveFile(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFile());
			} else if (citem.getFolder() != null) {
				if (citem.getOperation() == Clipboard.COPY)
					copyFolder(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFolder());
				else if (citem.getOperation() == Clipboard.CUT)
					moveFolder(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFolder());
			} else if (citem.getFiles() != null)
				if (citem.getOperation() == Clipboard.COPY)
					copyFiles(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFiles());
				else if (citem.getOperation() == Clipboard.CUT)
					moveFiles(GSS.get().getCurrentUser().getId(), (FolderDTO) selection, citem.getFiles());

		} else if (selection instanceof FileHeaderDTO) {
			// TODO do we paste in containing folder?
		} else if (selection instanceof UserDTO) {
			// TODO nothing
		} else if (selection instanceof GroupDTO) {
			ClipboardItem citem = GSS.get().getClipboard().getItem();
			if (citem.getUser() != null)
				addUser();
		}
	}

	/**
	 * @param l
	 * @param selection
	 * @param file
	 */
	protected void copyFile(final Long userId, final FolderDTO selection, final FileHeaderDTO fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		service.copyFile(userId, fileToCopy.getId(), selection.getId(), fileToCopy.getName(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
				GSS.get().getStatusPanel().updateStats();
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
	 * @param l
	 * @param selection
	 * @param file
	 */
	protected void copyFiles(final Long userId, final FolderDTO selection, final List<FileHeaderDTO> fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		final List<Long> fids = new ArrayList<Long>();
		for (FileHeaderDTO f : fileToCopy)
			fids.add(f.getId());
		service.copyFiles(userId, fids, selection.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
				GSS.get().getStatusPanel().updateStats();
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
	 * @param l
	 * @param selection
	 * @param file
	 */
	protected void moveFiles(final Long userId, final FolderDTO selection, final List<FileHeaderDTO> fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		final List<Long> fids = new ArrayList<Long>();
		for (FileHeaderDTO f : fileToCopy)
			fids.add(f.getId());
		service.moveFiles(userId, fids, selection.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
				GSS.get().getStatusPanel().updateStats();
				GSS.get().getClipboard().clear();
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

	protected void copyFolder(final Long userId, final FolderDTO selection, final FolderDTO folderToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();

		service.copyFolderStructure(userId, folderToCopy.getId(), selection.getId(), folderToCopy.getName(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFolders().onFolderCopy(folder);
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

	protected void moveFile(final Long userId, final FolderDTO selection, final FileHeaderDTO fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		service.moveFile(userId, fileToCopy.getId(), selection.getId(), fileToCopy.getName(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
				GSS.get().getStatusPanel().updateStats();
				GSS.get().getClipboard().clear();
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

	protected void moveFolder(final Long userId, final FolderDTO selection, final FolderDTO folderToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder = GSS.get().getFolders().getCurrent();
		final Long folderId = ((FolderDTO) folder.getUserObject()).getId();
		final FolderDTO selectionParent = folderToCopy.getParent();
		service.moveFolder(userId, folderToCopy.getId(), selection.getId(), folderToCopy.getName(), new AsyncCallback() {

			public void onSuccess(final Object result) {

				GSS.get().getFolders().onFolderMove(folder, selectionParent);
				GSS.get().getClipboard().clear();
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

	private void addUser() {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		GroupDTO group = (GroupDTO) GSS.get().getCurrentSelection();
		UserDTO selectedUser = GSS.get().getClipboard().getItem().getUser();
		if (group == null) {
			GSS.get().displayError("Empty group name!");
			return;
		}
		if (selectedUser == null) {
			GSS.get().displayError("No User Selected!");
			return;
		}
		service.addUserToGroup(GSS.get().getCurrentUser().getId(), group.getId(), selectedUser.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getGroups().updateGroups(GSS.get().getCurrentUser().getId());
				GSS.get().showUserList();
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
