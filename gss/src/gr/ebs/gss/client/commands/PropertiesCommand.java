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
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.ExecuteHead;
import gr.ebs.gss.client.rest.ExecuteMultipleGet;
import gr.ebs.gss.client.rest.ExecuteMultipleHead;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupsResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
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

	private List<GroupResource> groups = null;

	private List<FileResource> versions = null;

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
		if (groups == null || versions == null)
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
		// Object selection = GSS.get().getCurrentSelection();
		// if (selection == null)
		// return;
		// GWT.log("selection: " + selection.toString(), null);
		if (GSS.get().getCurrentSelection() instanceof FolderResource) {
			ExecuteGet<FolderResource> eg = new ExecuteGet<FolderResource>(FolderResource.class, ((FolderResource)GSS.get().getCurrentSelection()).getPath()){

				@Override
				public void onComplete() {
					GSS.get().setCurrentSelection(getResult());
					FolderPropertiesDialog dlg = new FolderPropertiesDialog(propImages, false, groups);
					dlg.center();
				}
				@Override
				public void onError(Throwable t) {
					// TODO Auto-generated method stub

				}

			};
			DeferredCommand.addCommand(eg);
		}
		if (GSS.get().getCurrentSelection() instanceof FileResource) {
			ExecuteHead<FileResource> eg = new ExecuteHead<FileResource>(FileResource.class, ((FileResource)GSS.get().getCurrentSelection()).getPath()){

				@Override
				public void onComplete() {
					GSS.get().setCurrentSelection(getResult());
					FilePropertiesDialog dlg = new FilePropertiesDialog(propImages, groups, versions);
					dlg.center();
				}
				@Override
				public void onError(Throwable t) {
					// TODO Auto-generated method stub

				}

			};
			DeferredCommand.addCommand(eg);

		}

	}

	private void getGroups() {
		ExecuteGet<GroupsResource> gg = new ExecuteGet<GroupsResource>(GroupsResource.class, GSS.get().getCurrentUserResource().getGroupsPath()) {

			public void onComplete() {
				GroupsResource res = getResult();
				ExecuteMultipleGet<GroupResource> ga = new ExecuteMultipleGet<GroupResource>(GroupResource.class, res.getGroupPaths().toArray(new String[] {})) {

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

	private void getVersions() {
		if (GSS.get().getCurrentSelection() instanceof FileResource) {
			FileResource afile = (FileResource) GSS.get().getCurrentSelection();
			if (afile.isVersioned()) {
				List<String> paths = new ArrayList<String>();
				for (int i = 0; i < afile.getVersion(); i++)
					paths.add(afile.getPath() + "?version=" + i);
				ExecuteMultipleHead<FileResource> gv = new ExecuteMultipleHead<FileResource>(FileResource.class, paths.toArray(new String[] {})){
					public void onComplete() {
						versions = getResult();
					}
					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch versions");
						versions = new ArrayList<FileResource>();
					}

					public void onError(String p, Throwable throwable) {
						GWT.log("Path:"+p, throwable);
					}
				};
				DeferredCommand.addCommand(gv);
			} else
				versions = new ArrayList<FileResource>();

		} else
			versions = new ArrayList<FileResource>();

	}
}
