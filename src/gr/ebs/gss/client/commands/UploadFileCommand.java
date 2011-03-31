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

import gr.ebs.gss.client.FileUploadDialog;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Upload a file command
 *
 * @author kman
 */
public class UploadFileCommand implements Command {

	private PopupPanel containerPanel;
	private List<FileResource> files;

	public UploadFileCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		if(containerPanel!=null)
			containerPanel.hide();
		displayNewFile();
	}

	/**
	 * Display the 'new file' dialog for uploading a new file to the system.
	 */
	private void displayNewFile() {
		RestResource currentFolder = GSS.get().getTreeView().getSelection();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		getFileList();
		DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					FileUploadDialog dlg = GWT.create(FileUploadDialog.class);
					dlg.setFiles(files);
					dlg.center();
					return false;
				}
				return true;
			}

		});
	}

	private boolean canContinue() {
		if (files != null )
			return true;
		return false;
	}

	private void getFileList() {
		GetCommand<FolderResource> eg = new GetCommand<FolderResource>(FolderResource.class,((RestResourceWrapper)GSS.get().getTreeView().getSelection()).getUri(), null){

			@Override
			public void onComplete() {
				files = getResult().getFiles();
			}

			@Override
			public void onError(Throwable t) {
				files = new ArrayList<FileResource>();
			}

		};
		DeferredCommand.addCommand(eg);
	}

}
