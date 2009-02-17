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
package gr.ebs.gss.client.dnd;

import gr.ebs.gss.client.Folders;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.GSSServiceAsync;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class DnDFolderPopupMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Folders.Images images;

	public DnDFolderPopupMenu(final Folders.Images newImages, final FolderDTO target, final Object toCopy, boolean othersShared) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		// A dummy command that we will execute from unimplemented leaves.
		final Command cancelCmd = new Command() {

			public void execute() {
				hide();
			}
		};

		final MenuBar contextMenu = new MenuBar(true);
		final Folders folders = GSS.get().getFolders();
		if (!othersShared)
			contextMenu.addItem("<span>" + newImages.cut().getHTML() + "&nbsp;Move</span>", true, new Command() {

				public void execute() {
					if (toCopy instanceof FolderDTO)
						cutFolder(GSS.get().getCurrentUser().getId(), target, (FolderDTO) toCopy);
					else if (toCopy instanceof List)
						moveFiles(GSS.get().getCurrentUser().getId(), target, (List<FileHeaderDTO>) toCopy);
					hide();
				}

			}).setVisible(target != null);
		;
		contextMenu.addItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new Command() {

			public void execute() {
				if (toCopy instanceof FolderDTO)
					copyFolder(GSS.get().getCurrentUser().getId(), target, (FolderDTO) toCopy);
				else if (toCopy instanceof List)
					copyFiles(GSS.get().getCurrentUser().getId(), target, (List<FileHeaderDTO>) toCopy);

				hide();
			}

		}).setVisible(target != null);
		;
		contextMenu.addItem("<span>" + newImages.trash().getHTML() + "&nbsp;Delete (Trash)</span>", true, new Command() {

			public void execute() {
				if (toCopy instanceof FolderDTO && target == null)
					moveFolderToTrash(GSS.get().getCurrentUser().getId(), (FolderDTO) toCopy);
				else if (toCopy instanceof List)
					moveFilesToTrash(GSS.get().getCurrentUser().getId(), (List<FileHeaderDTO>) toCopy);
				hide();
			}

		}).setVisible(target == null);
		contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Cancel</span>", true, cancelCmd);

		add(contextMenu);

	}

	protected void copyFiles(final Long userId, final FolderDTO selection, final List<FileHeaderDTO> fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
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

	protected void moveFiles(final Long userId, final FolderDTO selection, final List<FileHeaderDTO> fileToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
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

	protected void moveFilesToTrash(final Long userId, final List<FileHeaderDTO> files) {
		final List<Long> fileIds = new ArrayList<Long>();
		for (FileHeaderDTO f : files)
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

	protected void cutFolder(final Long userId, final FolderDTO selection, final FolderDTO folderToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder;
		TreeItem folderTemp = GSS.get().getFolders().getUserItem(selection);
		if (folderTemp == null)
			folder = GSS.get().getFolders().getOtherSharedItem(selection);
		else
			folder = folderTemp;
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

	protected void copyFolder(final Long userId, final FolderDTO selection, final FolderDTO folderToCopy) {
		GSS.get().showLoadingIndicator();
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem folder;
		TreeItem folderTemp = GSS.get().getFolders().getUserItem(selection);
		if (folderTemp == null)
			folder = GSS.get().getFolders().getOtherSharedItem(selection);
		else
			folder = folderTemp;

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

	protected void moveFolderToTrash(Long userId, final FolderDTO selection) {
		FolderDTO fdto = selection;
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
	}

}
