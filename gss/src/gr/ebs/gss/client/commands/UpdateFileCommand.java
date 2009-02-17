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

import gr.ebs.gss.client.FileUpdateDialog;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.domain.FileHeaderDTO;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Update File Contents
 * @author kman
 *
 */
public class UpdateFileCommand implements Command{
	private PopupPanel containerPanel;
	boolean monitorCall = false;
	public UpdateFileCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selected = GSS.get().getCurrentSelection();
		if(selected == null)
			return;
		if(selected instanceof FileHeaderDTO)
			displayNewFile();
	}

	/**
	 * Display the 'new file' dialog for uploading a new file to
	 * the system.
	 */
	void displayNewFile() {
		DeferredCommand.addCommand(new Command() {

			public void execute() {

					FileUpdateDialog dlg = new FileUpdateDialog();
					dlg.center();
			}

		});

	}

}
