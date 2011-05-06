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
package org.gss_project.gss.web.client.commands;

import org.gss_project.gss.web.client.DeleteFileDialog;
import org.gss_project.gss.web.client.DeleteFolderDialog;
import org.gss_project.gss.web.client.DeleteGroupDialog;
import org.gss_project.gss.web.client.EditMenu.Images;
import org.gss_project.gss.web.client.GSS;
import org.gss_project.gss.web.client.rest.resource.FileResource;
import org.gss_project.gss.web.client.rest.resource.GroupResource;
import org.gss_project.gss.web.client.rest.resource.GroupUserResource;
import org.gss_project.gss.web.client.rest.resource.RestResourceWrapper;

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
