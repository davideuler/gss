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
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.ArrayList;
import java.util.List;

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

	List<FileResource> files;
	boolean monitorCall = false;
	public UploadFileCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		displayNewFile();
	}

	/**
	 * Display the 'new file' dialog for uploading a new file to the system.
	 */
	void displayNewFile() {
		TreeItem currentFolder = GSS.get().getFolders().getCurrent();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		getFileList();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					FileUploadDialog dlg = new FileUploadDialog(files);
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
		ExecuteGet<FolderResource> eg = new ExecuteGet<FolderResource>(FolderResource.class,((FolderResource)GSS.get().getFolders().getCurrent().getUserObject()).getPath()){

			public void onComplete() {
				files = getResult().getFiles();
			}

			public void onError(Throwable t) {
				files = new ArrayList<FileResource>();
			}

		};
		DeferredCommand.addCommand(eg);

	}


}
