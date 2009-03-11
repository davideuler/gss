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
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecuteMultiplePost;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


/**
 *
 * Move file or folder to trash
 * @author kman
 *
 */
public class ToTrashCommand implements Command{
	private PopupPanel containerPanel;

	public ToTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FolderResource) {
			FolderResource fdto = (FolderResource) selection;
			ExecutePost tot = new ExecutePost(fdto.getPath()+"?trash=","",200){

				public void onComplete() {
					TreeItem folder = GSS.get().getFolders().getCurrent();
					GSS.get().getFolders().updateFolder((DnDTreeItem) folder.getParentItem());
					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
				}

				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("Folder does not exist");
						else
							GSS.get().displayError("Unable to trash folder, status code:"+statusCode);
					}
					else
						GSS.get().displayError("System error trashing folder:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);
		} else if (selection instanceof FileResource) {
			FileResource fdto = (FileResource) selection;
			ExecutePost tot = new ExecutePost(fdto.getPath()+"?trash=","",200){

				public void onComplete() {
					GSS.get().showFileList(true);
				}

				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File does not exist");
						else
							GSS.get().displayError("Unable to trash file, status code:"+statusCode);
					}
					else
						GSS.get().displayError("System error trashing file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);

		}
		else if (selection instanceof List) {
			List<FileResource> fdtos = (List<FileResource>) selection;
			final List<String> fileIds = new ArrayList<String>();
			for(FileResource f : fdtos)
				fileIds.add(f.getPath()+"?trash=");
			ExecuteMultiplePost tot = new ExecuteMultiplePost(fileIds.toArray(new String[0]),200){

				public void onComplete() {
					GSS.get().showFileList(true);
				}


				public void onError(String p, Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File does not exist");
						else
							GSS.get().displayError("Unable to trash file, status code:"+statusCode);
					}
					else
						GSS.get().displayError("System error trashing file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);

		}

	}

}
