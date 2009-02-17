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

import gr.ebs.gss.client.FileMenu;
import gr.ebs.gss.client.FilePropertiesDialog;
import gr.ebs.gss.client.FolderPropertiesDialog;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.FileMenu.Images;
import gr.ebs.gss.client.domain.FileBodyDTO;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.PermissionDTO;
import gr.ebs.gss.client.domain.UserDTO;
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

/**
 * The command that displays the appropriate Properties dialog, according to the
 * selected object in the application.
 *
 * @author kman
 */
public class PropertiesCommand implements Command {

	final FileMenu.Images newImages;

	private PopupPanel containerPanel;

	private Set<PermissionDTO> permissions = null;

	private List<GroupDTO> groups = null;

	private List<FileBodyDTO> versions = null;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public PropertiesCommand(PopupPanel _containerPanel, final FileMenu.Images _newImages) {
		containerPanel = _containerPanel;
		newImages = _newImages;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		getPermissions();
		getGroups();
		getVersions();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					displayProperties(newImages);
					return false;
				}
				return true;

			}

		});

	}

	private boolean canContinue() {
		if (permissions == null || groups == null || versions == null)
			return false;
		return true;
	}

	/**
	 * Display the appropriate Properties dialog, according to the selected
	 * object in the application.
	 *
	 * @param propImages the images of all the possible properties dialogs
	 */
	void displayProperties(final Images propImages) {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FolderDTO) {
			FolderPropertiesDialog dlg = new FolderPropertiesDialog(propImages, false, permissions, groups);
			dlg.center();
		} else if (selection instanceof FileHeaderDTO) {
			FilePropertiesDialog dlg = new FilePropertiesDialog(propImages, permissions, groups, versions);
			dlg.center();
		} else if (selection instanceof UserDTO) {
			// TODO implement user properties
		} else if (selection instanceof GroupDTO) {
			// TODO implement group properties
		}
	}

	private void getPermissions() {
		if (GSS.get().getCurrentSelection() instanceof FolderDTO)
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
		else if (GSS.get().getCurrentSelection() instanceof FileHeaderDTO)
			GSS	.get()
				.getRemoteService()
				.getFilePermissions(GSS.get().getCurrentUser().getId(), ((FileHeaderDTO) GSS.get().getCurrentSelection()).getId(), new AsyncCallback() {

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

		GSS.get().getRemoteService().getGroups(GSS.get().getCurrentUser().getId(), new AsyncCallback() {

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

	private void getVersions() {
		if (GSS.get().getCurrentSelection() instanceof FileHeaderDTO)
			GSS	.get()
				.getRemoteService()
				.getVersions(GSS.get().getCurrentUser().getId(), ((FileHeaderDTO) GSS.get().getCurrentSelection()).getId(), new AsyncCallback() {

					public void onFailure(Throwable caught) {
						GWT.log("", caught);
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
						// initialize list object so that we avoid infinite loop
						versions = new ArrayList<FileBodyDTO>();
					}

					public void onSuccess(Object result) {
						versions = (List<FileBodyDTO>) result;
					}

				});
		else
			versions = new ArrayList<FileBodyDTO>();

	}
}
