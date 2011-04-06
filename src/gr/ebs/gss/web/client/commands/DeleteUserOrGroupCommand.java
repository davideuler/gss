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
package gr.ebs.gss.web.client.commands;

import gr.ebs.gss.web.client.DeleteGroupDialog;
import gr.ebs.gss.web.client.DeleteUserDialog;
import gr.ebs.gss.web.client.GSS;
import gr.ebs.gss.web.client.Groups.Images;
import gr.ebs.gss.web.client.rest.resource.GroupResource;
import gr.ebs.gss.web.client.rest.resource.GroupUserResource;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * @author kman
 *
 */
public class DeleteUserOrGroupCommand implements Command{
	private PopupPanel containerPanel;
	final Images images;

	/**
	 * @param aContainerPanel
	 * @param newImages the images of the new folder dialog
	 */
	public DeleteUserOrGroupCommand(PopupPanel aContainerPanel, final Images newImages){
		containerPanel = aContainerPanel;
		images = newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		if(GSS.get().getCurrentSelection() instanceof GroupResource)
			displayNewGroup();
		else if(GSS.get().getCurrentSelection() instanceof GroupUserResource)
			displayNewUser();
		else
			GSS.get().displayError("No user or group selected");
	}

	void displayNewGroup() {
		DeleteGroupDialog dlg = new DeleteGroupDialog(images);
		dlg.center();
	}

	void displayNewUser() {
		DeleteUserDialog dlg = new DeleteUserDialog(images);
		dlg.center();
	}

}
