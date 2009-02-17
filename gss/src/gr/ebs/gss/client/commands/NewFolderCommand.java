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

import gr.ebs.gss.client.FolderPropertiesDialog;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.FileMenu.Images;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


/**
 * Display the 'new folder' dialog for creating a new folder.
 * @author kman
 *
 */
public class NewFolderCommand implements Command{
	private PopupPanel containerPanel;
	final Images newImages;
	private Set<PermissionDTO> permissions = null;
	private List<GroupDTO> groups = null;
	/**
	 * @param _containerPanel
	 * @param _@param newImages the images of the new folder dialog
	 */
	public NewFolderCommand(PopupPanel _containerPanel, final Images _newImages){
		containerPanel = _containerPanel;
		newImages=_newImages;
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		getPermissions();
		getGroups();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					displayNewFolder();
					return false;
				}
				return true;

			}

		});

	}

	private boolean canContinue() {
		if (permissions == null || groups ==null)
			return false;
		return true;
	}

	void displayNewFolder() {
		TreeItem currentFolder = GSS.get().getFolders().getCurrent();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		FolderPropertiesDialog dlg = new FolderPropertiesDialog(newImages, true, permissions, groups);
		dlg.center();
	}

	private void getPermissions() {

		GSS	.get()
			.getRemoteService()
			.getFolderPermissions(GSS.get().getCurrentUser().getId(), ((FolderDTO) GSS.get().getFolders().getCurrent().getUserObject()).getId(), new AsyncCallback() {

				public void onFailure(Throwable caught) {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
					// initialize list object so that we avoid infinite loop
					permissions = new HashSet<PermissionDTO>();
				}

				public void onSuccess(Object result) {
					permissions = (Set<PermissionDTO>) result;
				}

			});

	}

	private void getGroups() {

		GSS	.get()
			.getRemoteService()
			.getGroups(GSS.get().getCurrentUser().getId(), new AsyncCallback() {

				public void onFailure(Throwable caught) {
					GWT.log("", caught);
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
					// initialize list object so that we avoid infinite loop
					groups = new ArrayList<GroupDTO>();
				}

				public void onSuccess(Object result) {
					groups = (List<GroupDTO>) result;
				}

			});

	}

}
