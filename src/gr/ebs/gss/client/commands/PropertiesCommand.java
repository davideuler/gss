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
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.HeadCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.MultipleHeadCommand;
import gr.ebs.gss.client.rest.RestException;
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

	private int tabToShow = 0;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 * @param _tab the tab to switch to
	 */
	public PropertiesCommand(PopupPanel _containerPanel, final FileMenu.Images _newImages, int _tab) {
		containerPanel = _containerPanel;
		newImages = _newImages;
		tabToShow = _tab;
	}

	public void execute() {
		containerPanel.hide();
		if (GSS.get().getCurrentSelection() instanceof FolderResource) {
			GetCommand<FolderResource> eg = new GetCommand<FolderResource>(FolderResource.class, ((FolderResource) GSS.get().getCurrentSelection()).getUri()) {

				@Override
				public void onComplete() {
					GSS.get().setCurrentSelection(getResult());
					initialize();
				}

				@Override
				public void onError(Throwable t) {

				}

			};
			DeferredCommand.addCommand(eg);
		}
		else if (GSS.get().getCurrentSelection() instanceof FileResource) {
			final String path = ((FileResource) GSS.get().getCurrentSelection()).getUri();
			// Needed because firefox caches head requests.
			HeadCommand<FileResource> eg = new HeadCommand<FileResource>(FileResource.class, path+"?"+Math.random() ) {

				@Override
				public void onComplete() {
					FileResource res = getResult();
					res.setUri(path);
					GSS.get().setCurrentSelection(res);
					initialize();
				}

				@Override
				public void onError(Throwable t) {
					if(t instanceof RestException)
						GSS.get().displayError("Unable to retrieve file details:"+((RestException)t).getHttpStatusText());
				}

			};
			DeferredCommand.addCommand(eg);
		}
	}

	private void initialize(){
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
		if (GSS.get().getCurrentSelection() instanceof FolderResource) {
			FolderPropertiesDialog dlg = new FolderPropertiesDialog(propImages, false, groups);
			dlg.selectTab(tabToShow);
			dlg.center();
		} else if (GSS.get().getCurrentSelection() instanceof FileResource) {
			FilePropertiesDialog dlg = new FilePropertiesDialog(propImages, groups, versions);
			dlg.selectTab(tabToShow);
			dlg.center();
		}
	}

	private void getGroups() {
		GetCommand<GroupsResource> gg = new GetCommand<GroupsResource>(GroupsResource.class, GSS.get().getCurrentUserResource().getGroupsPath()) {

			@Override
			public void onComplete() {
				GroupsResource res = getResult();
				MultipleGetCommand<GroupResource> ga = new MultipleGetCommand<GroupResource>(GroupResource.class, res.getGroupPaths().toArray(new String[] {})) {

					@Override
					public void onComplete() {
						List<GroupResource> groupList = getResult();
						groups = groupList;
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch groups");
						groups = new ArrayList<GroupResource>();
					}

					@Override
					public void onError(String p, Throwable throwable) {
						GWT.log("Path:" + p, throwable);
					}
				};
				DeferredCommand.addCommand(ga);
			}

			@Override
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
			GWT.log("File is versioned:" + afile.isVersioned(), null);
			if (afile.isVersioned()) {
				List<String> paths = new ArrayList<String>();
				for (int i = 1; i <= afile.getVersion(); i++)
					paths.add(afile.getUri() + "?version=" + i);
				MultipleHeadCommand<FileResource> gv = new MultipleHeadCommand<FileResource>(FileResource.class, paths.toArray(new String[] {})) {

					@Override
					public void onComplete() {
						versions = getResult();
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch versions");
						versions = new ArrayList<FileResource>();
					}

					@Override
					public void onError(String p, Throwable throwable) {
						GWT.log("Path:" + p, throwable);
					}
				};
				DeferredCommand.addCommand(gv);
			} else
				versions = new ArrayList<FileResource>();
		} else
			versions = new ArrayList<FileResource>();
	}
}
