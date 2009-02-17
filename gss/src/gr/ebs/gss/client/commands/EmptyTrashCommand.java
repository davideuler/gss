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

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.exceptions.RpcException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command To Empty Trashbin
 * @author kman
 *
 */
public class EmptyTrashCommand implements Command{
	private PopupPanel containerPanel;

	public EmptyTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();

		GSS.get().getRemoteService().emptyTrash(GSS.get().getCurrentUser().getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getTrashItem());
				GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getMySharesItem());
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
				GSS.get().getStatusPanel().updateStats();
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});
	}

}
