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

import gr.ebs.gss.client.DeleteFileDialog;
import gr.ebs.gss.client.DeleteFolderDialog;
import gr.ebs.gss.client.DeleteGroupDialog;
import gr.ebs.gss.client.EditMenu.Images;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Delete selected object command
 * @author kman
 *
 */
public class DeleteCommand implements Command{
	private PopupPanel containerPanel;
	final Images newImages;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public DeleteCommand( PopupPanel _containerPanel, final Images _newImages ){
		containerPanel = _containerPanel;
		newImages=_newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		displayDelete();
	}
	/**
	 * Display the delete dialog, according to the selected object.
	 *
	 *
	 */
	void displayDelete() {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof RestResourceWrapper) {
			DeleteFolderDialog dlg = new DeleteFolderDialog(newImages);
			dlg.center();
		} else if (selection instanceof FileResource || selection instanceof List) {
			DeleteFileDialog dlg = new DeleteFileDialog(newImages);
			dlg.center();
		} else if (selection instanceof GroupUserResource) {
			// TODO implement user deletion
		} else if (selection instanceof GroupResource) {
			DeleteGroupDialog dlg = new DeleteGroupDialog(newImages);
			dlg.center();
		}
	}
}
