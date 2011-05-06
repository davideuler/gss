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

import org.gss_project.gss.web.client.GSS;
import org.gss_project.gss.web.client.rest.DeleteCommand;
import org.gss_project.gss.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command to empty trash bin.
 *
 * @author kman
 */
public class EmptyTrashCommand implements Command{
	private PopupPanel containerPanel;

	public EmptyTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		DeleteCommand df = new DeleteCommand(GSS.get().getTreeView().getTrash().getUri()){

			@Override
			public void onComplete() {
				GSS.get().getTreeView().updateTrashNode();
				GSS.get().showFileList(true);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("Resource does not exist");
					else
						GSS.get().displayError("Unable to empty trash:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error emptying trash:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(df);
	}

}
