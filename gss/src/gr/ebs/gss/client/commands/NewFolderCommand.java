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
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.ExecuteMultipleGet;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupsResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
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

	private List<GroupResource> groups = null;
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
		if (groups == null)
			return false;
		return true;
	}

	void displayNewFolder() {
		TreeItem currentFolder = GSS.get().getFolders().getCurrent();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		FolderPropertiesDialog dlg = new FolderPropertiesDialog(newImages, true,  groups);
		dlg.center();
	}



	private void getGroups() {
		ExecuteGet<GroupsResource> gg = new ExecuteGet<GroupsResource>(GroupsResource.class, GSS.get().getCurrentUserResource().getGroupsPath()){

			public void onComplete() {
				GroupsResource res = getResult();
				ExecuteMultipleGet<GroupResource> ga = new ExecuteMultipleGet<GroupResource>(GroupResource.class, res.getGroupPaths().toArray(new String[]{})){

					public void onComplete() {
						List<GroupResource> groupList = getResult();
						groups = groupList;
					}


					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch groups");
						groups = new ArrayList<GroupResource>();
					}

					public void onError(String p, Throwable throwable) {
						GWT.log("Path:"+p, throwable);
					}
				};
				DeferredCommand.addCommand(ga);
			}


			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch groups");
				groups = new ArrayList<GroupResource>();
			}
		};
		DeferredCommand.addCommand(gg);


	}

}
